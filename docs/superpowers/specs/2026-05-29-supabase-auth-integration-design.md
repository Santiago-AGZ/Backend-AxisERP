# Supabase Auth Integration — Design Doc

## Problem

Admin creates a user via `POST /api/v1/usuarios`. Currently this only creates a profile in Neon's `auth_service.profiles` table. The user must be created separately in Supabase Auth (manual two-step process).

## Solution

Create a new output port `SupabaseAuthPort` and adapter `SupabaseAdminAdapter` that calls Supabase Admin API. The `CreateUserUseCaseImpl` calls it inside the existing `@Transactional` flow. If Supabase fails, the profile is never saved (natural rollback). The first login flow promotes `PENDIENTE` → `ACTIVO`.

## Architecture

```
ports/output/SupabaseAuthPort.java          ← interfaz pura (sin Spring)
ports/output/SupabaseUser.java              ← record inmutable

infrastructure/adapters/out/supabase/
  SupabaseAdminAdapter.java                 ← implementa SupabaseAuthPort
  SupabaseAdminAdapterTest.java

domain/model/User.java                      ← UserStatus.PENDIENTE (ya existe)

application/usecase/
  CreateUserUseCaseImpl.java                ← modificado: llama a SupabaseAuthPort

infrastructure/adapters/in/web/controller/
  UserController.java                       ← sin cambios

infrastructure/config/
  FirstLoginFilter.java                     ← filtro que actualiza PENDIENTE → ACTIVO
```

## Component Design

### 1. `SupabaseAuthPort` (ports/output)

```java
package com.axiserp.auth.ports.output;

import java.util.UUID;
import com.axiserp.auth.domain.model.UserRole;

public interface SupabaseAuthPort {
    SupabaseUser createUser(String email, String roleName, String name, UUID createdBy);
}

public record SupabaseUser(UUID id, String email, java.time.Instant invitedAt) {}
```

- Sin imports de Spring, HTTP, o infraestructura
- `roleName` es el `String` del nombre del rol (ej: "ADMIN", "VENDEDOR")

### 2. `SupabaseAdminAdapter` (infrastructure/adapters/out/supabase)

```java
@Component
public class SupabaseAdminAdapter implements SupabaseAuthPort {

    private final RestClient restClient;

    public SupabaseAdminAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {

        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/auth/v1/admin")
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public SupabaseUser createUser(String email, String roleName, String name, UUID createdBy) {
        var body = Map.of(
            "email", email,
            "app_metadata", Map.of("role", roleName),
            "user_metadata", Map.of("name", name, "createdBy", createdBy.toString()),
            "email_confirm", false
        );

        var response = restClient.post()
                .uri("/users")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return new SupabaseUser(
                UUID.fromString(response.get("id").asText()),
                response.get("email").asText(),
                Instant.parse(response.get("invited_at").asText()));
    }
}
```

- Sin password en el body → Supabase envía invite email automáticamente
- `RestClient` de Spring Boot 3.5 (sin dependencias nuevas)
- Se inyectan `SUPABASE_URL` y `SUPABASE_SERVICE_ROLE_KEY` desde properties

### 3. `CreateUserUseCaseImpl` modificado

```java
@Override
@Transactional
public UserResponse create(CreateUserRequest request, UUID adminId) {
    if (userRepositoryPort.existsByEmail(request.getEmail())) {
        throw new DuplicateEmailException();
    }

    var role = roleRepositoryPort.findByName(request.getRole())
            .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.getRole()));

    // 1. Crear en Supabase (lanza excepción si falla → rollback de todo)
    SupabaseUser supabaseUser = supabaseAuthPort.createUser(
            request.getEmail(), role.getName(), request.getName(), adminId);

    // 2. Crear perfil en Neon con el UUID de Supabase
    User user = UserFactory.createNew(
            supabaseUser.id(),        ← UUID de Supabase (NO generado localmente)
            request.getName(),
            request.getEmail(),
            role.getId(),
            adminId);
    user.setStatus(UserStatus.PENDIENTE);  ← arranca pendiente hasta confirmar email

    User saved = userRepositoryPort.save(user);

    auditService.log(AuditAction.CREATE, "USER", saved.getId(),
            adminId, null,
            Map.of("email", saved.getEmail(), "role", request.getRole(), "supabaseId", supabaseUser.id()),
            null, null);

    return UserResponse.builder()
            .id(saved.getId())
            .name(saved.getName())
            .email(saved.getEmail())
            .role(request.getRole())
            .status(saved.getStatus().name())
            .createdAt(saved.getCreatedAt())
            .build();
}
```

### 4. `UserFactory` modificado

`UserFactory.createNew` ya no recibe `UUID roleId` como único UUID — ahora recibe el UUID de Supabase:

```java
public static User createNew(UUID id, String name, String email, UUID roleId, UUID createdBy) {
    return User.builder()
            .id(id)                     ← UUID de Supabase Auth
            .name(name)
            .email(email)
            .passwordHash("")
            .roleId(roleId)
            .status(UserStatus.PENDIENTE)
            .createdBy(createdBy)
            .failedLoginAttempts(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
}
```

### 5. `FirstLoginFilter` (infrastructure/config)

Filtro que intercepta cada request autenticado. Si el JWT tiene `email_confirmed_at` y el perfil está `PENDIENTE`, lo actualiza a `ACTIVO`:

```java
@Component
@RequiredArgsConstructor
public class FirstLoginFilter extends OncePerRequestFilter {

    private final UserRepositoryPort userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String userId) {
            Jwt jwt = (Jwt) auth.getCredentials();
            Instant emailConfirmed = jwt.getClaimAsInstant("email_confirmed_at");

            if (emailConfirmed != null) {
                userRepository.findById(UUID.fromString(userId))
                        .filter(user -> UserStatus.PENDIENTE.equals(user.getStatus()))
                        .ifPresent(user -> {
                            user.setStatus(UserStatus.ACTIVO);
                            userRepository.save(user);
                        });
            }
        }

        chain.doFilter(request, response);
    }
}
```

## Configuration (application.properties)

```properties
supabase.url=${SUPABASE_URL}
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
```

Ya existen en `.env`.

## Error Handling

| Error | Comportamiento |
|-------|---------------|
| Supabase Admin API 4xx (email duplicado) | Adaptador lanza `DuplicateEmailException` → `@Transactional` rollback → `GlobalExceptionHandler` devuelve 409 |
| Supabase Admin API 5xx | Adaptador lanza `SupabaseAuthException` (RuntimeException) → rollback → 502 |
| Supabase temporariamente caído | Resilience4j Circuit Breaker (ya configurado en `.env`) protege contra cascada |

## Testing

- `SupabaseAdminAdapterTest` — unit test mockeando `RestClient`
- `CreateUserUseCaseImplTest` — ya existe, agregar mock de `SupabaseAuthPort`
- `FirstLoginFilterTest` — test con JWT mockeado con/sin `email_confirmed_at`
- Test de integración opcional con Testcontainers + WireMock para Supabase

## Archivos a crear

1. `ports/output/SupabaseAuthPort.java`
2. `ports/output/SupabaseUser.java`
3. `infrastructure/adapters/out/supabase/SupabaseAdminAdapter.java`

## Archivos a modificar

4. `application/usecase/CreateUserUseCaseImpl.java` — agregar `SupabaseAuthPort`
5. `domain/factory/UserFactory.java` — recibir UUID externo, status PENDIENTE
6. `domain/model/User.java` — `UserStatus.PENDIENTE` ya existe ✅
7. `infrastructure/config/SecurityConfig.java` — agregar `FirstLoginFilter`
8. `infrastructure/config/FirstLoginFilter.java` — nuevo filtro
9. `application.properties` — agregar `supabase.url` y `supabase.service-role-key`
10. Test files

## Decisiones

- **Sin SDK de Supabase** — REST directo con `RestClient`, cero dependencias nuevas
- **Status PENDIENTE** — el usuario no puede operar hasta confirmar email
- **UUID compartido** — el perfil en Neon usa el mismo UUID que Supabase Auth
- **Sin webhook** — el primer login actualiza el status, más simple y robusto
