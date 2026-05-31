# Supabase Auth Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When admin creates a user via `POST /api/v1/usuarios`, auth-service automatically creates the user in Supabase Auth (triggering invite email) and creates the profile in Neon with the matching Supabase UUID.

**Architecture:** New `SupabaseAuthPort` (output port) + `SupabaseAdminAdapter` (HTTP adapter) in hexagonal style. `CreateUserUseCaseImpl` calls the port inside `@Transactional`. New `FirstLoginFilter` promotes `PENDIENTE` → `ACTIVO` when user logs in with confirmed email.

**Tech Stack:** Spring Boot 3.5, Spring `RestClient`, Supabase GoTrue Admin API, Testcontainers

---

### Task 1: Create SupabaseAuthPort interface

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/ports/output/SupabaseAuthPort.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/ports/output/SupabaseUser.java`

- [ ] **Step 1: Create `SupabaseUser` record**

```java
package com.axiserp.auth.ports.output;

import java.time.Instant;
import java.util.UUID;

public record SupabaseUser(UUID id, String email, Instant invitedAt) {}
```

- [ ] **Step 2: Create `SupabaseAuthPort` interface**

```java
package com.axiserp.auth.ports.output;

import java.util.UUID;

public interface SupabaseAuthPort {
    SupabaseUser createUser(String email, String roleName, String name, UUID createdBy);
}
```

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/ports/output/SupabaseAuthPort.java auth-service/src/main/java/com/axiserp/auth/ports/output/SupabaseUser.java
git commit -m "feat: add SupabaseAuthPort output port"
```

---

### Task 2: Add Supabase properties to application.properties

**Files:**
- Modify: `auth-service/src/main/resources/application.properties`
- Modify: `.env` (already has SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY)

- [ ] **Step 1: Add properties at end of application.properties**

```properties
# =========================================
# SUPABASE ADMIN API (user creation/invite)
# =========================================
supabase.url=${SUPABASE_URL}
supabase.service-role-key=${SUPABASE_SERVICE_ROLE_KEY}
```

- [ ] **Step 2: Commit**

```bash
git add auth-service/src/main/resources/application.properties
git commit -m "feat: add supabase admin api properties"
```

---

### Task 3: Implement SupabaseAdminAdapter

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/supabase/SupabaseAdminAdapter.java`

- [ ] **Step 1: Create the adapter**

```java
package com.axiserp.auth.infrastructure.adapters.out.supabase;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SupabaseAdminAdapter implements SupabaseAuthPort {

    private final RestClient restClient;

    public SupabaseAdminAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {

        String baseUrl = supabaseUrl + "/auth/v1/admin";

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
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
            "user_metadata", Map.of(
                "name", name,
                "createdBy", createdBy.toString()
            ),
            "email_confirm", false
        );

        JsonNode response = restClient.post()
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

- [ ] **Step 2: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/supabase/SupabaseAdminAdapter.java
git commit -m "feat: implement SupabaseAdminAdapter"
```

---

### Task 4: Modify UserFactory to accept Supabase UUID and set PENDIENTE

**Files:**
- Modify: `auth-service/src/main/java/com/axiserp/auth/domain/factory/UserFactory.java`

- [ ] **Step 1: Change `createNew` signature — add `UUID id` first param, change status to PENDIENTE**

```java
public static User createNew(UUID id, String name, String email,
                              UUID roleId, UUID createdBy) {
    return User.builder()
            .id(id)
            .name(name)
            .email(email)
            .passwordHash("")
            .roleId(roleId)
            .status(UserStatus.PENDIENTE)
            .createdBy(createdBy)
            .failedLoginAttempts(0)
            .lastLoginAt(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deletedAt(null)
            .build();
}
```

- [ ] **Step 2: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/domain/factory/UserFactory.java
git commit -m "feat: UserFactory accepts external UUID, defaults to PENDIENTE"
```

---

### Task 5: Modify CreateUserUseCaseImpl

**Files:**
- Modify: `auth-service/src/main/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImpl.java`

- [ ] **Step 1: Add `SupabaseAuthPort` field and update `create` method**

```java
package com.axiserp.auth.application.usecase;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.CreateUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditService auditService;
    private final SupabaseAuthPort supabaseAuthPort;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request, UUID createdBy) {
        if (userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        var role = roleRepositoryPort.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.getRole()));

        SupabaseUser supabaseUser = supabaseAuthPort.createUser(
                request.getEmail(), role.getName(), request.getName(), createdBy);

        User user = UserFactory.createNew(
                supabaseUser.id(),
                request.getName(),
                request.getEmail(),
                role.getId(),
                createdBy);

        User saved = userRepositoryPort.save(user);

        auditService.log(AuditAction.CREATE, "USER", saved.getId(),
                createdBy, null,
                Map.of("email", saved.getEmail(), "role", request.getRole(), "supabaseId", supabaseUser.id()),
                null, null);

        log.info("user_created id={} email={} role={} created_by={} supabase_id={}",
                saved.getId(), saved.getEmail(), request.getRole(), createdBy, supabaseUser.id());

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(request.getRole())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImpl.java
git commit -m "feat: integrate SupabaseAuthPort into CreateUserUseCaseImpl"
```

---

### Task 6: Update CreateUserUseCaseImplTest

**Files:**
- Modify: `auth-service/src/test/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImplTest.java`

- [ ] **Step 1: Add `SupabaseAuthPort` mock and update test**

```java
    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    private static final UUID SUPABASE_USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should create user successfully via Supabase Auth")
    void create_success() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "ADMIN");
        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();

        SupabaseUser supabaseUser = new SupabaseUser(SUPABASE_USER_ID, "test@axiserp.com", Instant.now());

        User savedUser = User.builder()
                .id(SUPABASE_USER_ID)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("")
                .roleId(roleId)
                .status(User.UserStatus.PENDIENTE)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(roleRepositoryPort.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(supabaseAuthPort.createUser("test@axiserp.com", "ADMIN", "Test User", adminId))
                .thenReturn(supabaseUser);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = createUserUseCase.create(request, adminId);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@axiserp.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("PENDIENTE", response.getStatus());
        verify(supabaseAuthPort).createUser("test@axiserp.com", "ADMIN", "Test User", adminId);
        verify(userRepositoryPort).save(any(User.class));
    }
```

Full updated file:

```java
package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private AuditService auditService;

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @InjectMocks
    private CreateUserUseCaseImpl createUserUseCase;

    private UUID adminId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create user successfully via Supabase Auth")
    void create_success() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "ADMIN");
        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();
        UUID supabaseId = UUID.randomUUID();
        SupabaseUser supabaseUser = new SupabaseUser(supabaseId, "test@axiserp.com", Instant.now());
        User savedUser = User.builder()
                .id(supabaseId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("")
                .roleId(roleId)
                .status(User.UserStatus.PENDIENTE)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(roleRepositoryPort.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(supabaseAuthPort.createUser("test@axiserp.com", "ADMIN", "Test User", adminId))
                .thenReturn(supabaseUser);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = createUserUseCase.create(request, adminId);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@axiserp.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("PENDIENTE", response.getStatus());
        verify(supabaseAuthPort).createUser("test@axiserp.com", "ADMIN", "Test User", adminId);
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email exists")
    void create_duplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("Test User", "existing@axiserp.com", "ADMIN");

        when(userRepositoryPort.existsByEmail("existing@axiserp.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> createUserUseCase.create(request, adminId));
        verify(userRepositoryPort, never()).save(any());
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when role is invalid")
    void create_invalidRole() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "INVALID");

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(roleRepositoryPort.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> createUserUseCase.create(request, adminId));
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -pl auth-service -Dtest=CreateUserUseCaseImplTest`
Expected: All 3 tests PASS

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImplTest.java
git commit -m "test: update CreateUserUseCaseImplTest with SupabaseAuthPort mock"
```

---

### Task 7: Create FirstLoginFilter

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/config/FirstLoginFilter.java`
- Create: `auth-service/src/test/java/com/axiserp/auth/infrastructure/config/FirstLoginFilterTest.java`

- [ ] **Step 1: Create the filter**

```java
package com.axiserp.auth.infrastructure.config;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
            if (auth.getCredentials() instanceof Jwt jwt) {
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
        }

        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 2: Register filter in SecurityConfig**

Add import and inject `FirstLoginFilter`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final FirstLoginFilter firstLoginFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this::handleUnauthorized))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(this::handleUnauthorized))
                .addFilterBefore(firstLoginFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
                .build();
    }
    // ...rest unchanged
}
```

- [ ] **Step 3: Create test for FirstLoginFilter**

```java
package com.axiserp.auth.infrastructure.config;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class FirstLoginFilterTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private FirstLoginFilter filter;

    @Test
    void shouldPromotePendingToActivoWhenEmailConfirmed() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email_confirmed_at", Instant.now().toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, java.util.List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        User pendingUser = User.builder()
                .id(userId)
                .status(UserStatus.PENDIENTE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

        verify(userRepository).findById(userId);
        verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVO));
    }

    @Test
    void shouldNotUpdateWhenStatusIsAlreadyActivo() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email_confirmed_at", Instant.now().toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, java.util.List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        User activoUser = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(activoUser));

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -pl auth-service -Dtest=FirstLoginFilterTest`
Expected: Both tests PASS

- [ ] **Step 5: Run all auth-service tests**

Run: `mvn test -pl auth-service`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/config/FirstLoginFilter.java auth-service/src/main/java/com/axiserp/auth/infrastructure/config/SecurityConfig.java auth-service/src/test/java/com/axiserp/auth/infrastructure/config/FirstLoginFilterTest.java
git commit -m "feat: add FirstLoginFilter to promote PENDIENTE to ACTIVO on confirmed login"
```

---

### Task 8: Final build and verify

- [ ] **Step 1: Build entire project**

Run: `mvn compile -pl auth-service -am`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run all tests**

Run: `mvn test -pl auth-service`
Expected: All tests PASS

- [ ] **Step 3: Commit any remaining files**

```bash
git add -A
git commit -m "chore: complete supabase auth integration"
```
