# Auth Service Security & Business Rules — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Close 6 security and business-rule gaps in auth-service: JIT provisioning, user status enforcement, use-case validations, rate limiting, password reset endpoint.

**Architecture:** Four independent additions to existing hexagonal architecture: (1) a filter chain for status enforcement + JIT provisioning, (2) use-case level validation, (3) rate limiting via Bucket4j, (4) password reset delegation to Supabase.

**Tech Stack:** Spring Boot 3.5, Bucket4j 8.x, Caffeine, Spring Security, Supabase Auth Admin API (GoTrue)

---

### Task 1: Add Bucket4j + Caffeine dependencies + CacheConfig

**Files:**
- Modify: `pom.xml`
- Create: `infrastructure/config/CacheConfig.java`

**Step 1: Add dependencies to pom.xml**

Add inside `<dependencies>` block (after existing actuator dependency):

```xml
		<!-- Bucket4j - Rate Limiting -->
		<dependency>
			<groupId>com.bucket4j</groupId>
			<artifactId>bucket4j-core</artifactId>
			<version>8.10.1</version>
		</dependency>

		<!-- Caffeine - Cache for rate limit buckets -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-cache</artifactId>
		</dependency>
		<dependency>
			<groupId>com.github.ben-manes.caffeine</groupId>
			<artifactId>caffeine</artifactId>
		</dependency>
```

**Step 2: Create CacheConfig.java**

Create `src/main/java/com/axiserp/auth/infrastructure/config/CacheConfig.java`:

```java
package com.axiserp.auth.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("rate-limit-buckets");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(1000));
        return manager;
    }
}
```

**Step 3: Verify compilation**

```bash
cd auth-service && .\mvnw.cmd clean compile -q
```
Expected: no output (success)

---

### Task 2: Create UserStatusFilter with JIT Provisioning

**Files:**
- Create: `infrastructure/config/UserStatusFilter.java`
- Create: `infrastructure/config/dto/JitProvisionResult.java`
- Test: `src/test/java/com/axiserp/auth/infrastructure/config/UserStatusFilterTest.java`

**Step 1: Create JIT Provision Result record**

Create `src/main/java/com/axiserp/auth/infrastructure/config/dto/JitProvisionResult.java`:

```java
package com.axiserp.auth.infrastructure.config.dto;

import com.axiserp.auth.domain.model.User;

public record JitProvisionResult(User user, boolean wasProvisioned) {}
```

**Step 2: Create UserStatusFilter**

Create `src/main/java/com/axiserp/auth/infrastructure/config/UserStatusFilter.java`:

```java
package com.axiserp.auth.infrastructure.config;

import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.infrastructure.config.dto.JitProvisionResult;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStatusFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserStatusFilter.class);

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String userId) {
            UUID uuid = UUID.fromString(userId);
            JitProvisionResult result = findOrProvision(uuid, auth);

            if (!result.user().isActive()) {
                throw new UserInactiveException(
                        "Usuario " + result.user().getStatus().name().toLowerCase()
                        + (result.user().getDeletedAt() != null ? " o eliminado" : "")
                        + ". No tiene permisos para acceder al sistema.");
            }
        }

        chain.doFilter(request, response);
    }

    private JitProvisionResult findOrProvision(UUID userId, Authentication auth) {
        var existing = userRepository.findById(userId);
        if (existing.isPresent()) {
            return new JitProvisionResult(existing.get(), false);
        }

        if (auth.getCredentials() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            String name = extractName(jwt);
            String roleName = extractRole(jwt);
            var role = roleRepository.findByName(roleName).orElse(null);
            UUID roleId = role != null ? role.getId() : null;

            User provisioned = UserFactory.createNew(userId, name, email, roleId, null);
            User saved = userRepository.save(provisioned);

            log.info("jit_provision id={} email={} role={}", saved.getId(), saved.getEmail(), roleName);
            return new JitProvisionResult(saved, true);
        }

        throw new RuntimeException("No se pudo determinar la identidad del usuario");
    }

    private String extractName(Jwt jwt) {
        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");
        if (userMetadata != null && userMetadata.containsKey("name")) {
            return (String) userMetadata.get("name");
        }
        return jwt.getClaimAsString("email");
    }

    private String extractRole(Jwt jwt) {
        try {
            Map<String, Object> appMetadata = jwt.getClaimAsMap("app_metadata");
            if (appMetadata != null && appMetadata.containsKey("role")) {
                return (String) appMetadata.get("role");
            }
        } catch (Exception e) {
            // default
        }
        return "INVENTARIO";
    }
}
```

**Step 3: Create UserStatusFilterTest**

Create `src/test/java/com/axiserp/auth/infrastructure/config/UserStatusFilterTest.java`:

```java
package com.axiserp.auth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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

import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UserStatusFilterTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private UserStatusFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsActiveUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User activeUser = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository).findById(userId);
    }

    @Test
    void blocksInactiveUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User inactiveUser = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserInactiveException.class, () ->
                filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                        (req, res) -> {}));
    }

    @Test
    void blocksDeletedUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User deletedUser = User.builder()
                .id(userId)
                .status(UserStatus.ELIMINADO)
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedUser));

        assertThrows(UserInactiveException.class, () ->
                filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                        (req, res) -> {}));
    }

    @Test
    void provisionsUserWhenNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "new@axiserp.com")
                .claim("user_metadata", java.util.Map.of("name", "New User"))
                .build();
        setAuthentication(userId, jwt);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        User saved = User.builder().id(userId).name("New User").email("new@axiserp.com").status(UserStatus.PENDIENTE).build();
        when(userRepository.save(any())).thenReturn(saved);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository).save(any());
    }

    @Test
    void skipsActuatorPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        filter.doFilterInternal(request, new MockHttpServletResponse(),
                (req, res) -> {});

        SecurityContextHolder.getContext().setAuthentication(null);
        // Should not throw despite no auth
    }

    private void setAuthentication(UUID userId, Jwt jwt) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, List.of()));
    }
}
```

**Step 4: Run tests**

```bash
cd auth-service && .\mvnw.cmd test -Dtest=UserStatusFilterTest -q
```
Expected: 5 tests pass

---

### Task 3: Register UserStatusFilter + FirstLoginFilter in SecurityConfig

**Files:**
- Modify: `infrastructure/config/SecurityConfig.java`

**Step 1: Inject UserStatusFilter and register in chain**

Add field after `firstLoginFilter`:

```java
    private final UserStatusFilter userStatusFilter;
```

Change the filter chain (replace the `.addFilterBefore(firstLoginFilter, ...)` line):

```java
                .addFilterBefore(firstLoginFilter, BearerTokenAuthenticationFilter.class)
                .addFilterBefore(userStatusFilter, BearerTokenAuthenticationFilter.class)
```

Final order: `FirstLoginFilter` → `UserStatusFilter` → `BearerTokenAuthenticationFilter`

**Step 2: Verify compilation**

```bash
cd auth-service && .\mvnw.cmd clean compile -q
```
Expected: no output

---

### Task 4: Add use case validations (status enforcement)

**Files:**
- Modify: `application/usecase/CreateUserUseCaseImpl.java`
- Modify: `application/usecase/UpdateUserUseCaseImpl.java`
- Modify: `application/usecase/DeactivateUserUseCaseImpl.java`
- Modify: `application/usecase/GetUserUseCaseImpl.java`
- Test: `src/test/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImplTest.java`
- Test: `src/test/java/com/axiserp/auth/application/usecase/UpdateUserUseCaseImplTest.java` (if exists)
- Test: `src/test/java/com/axiserp/auth/application/usecase/DeactivateUserUseCaseImplTest.java`

**Step 1: Add UserNotFoundException**

Create `src/main/java/com/axiserp/auth/domain/exception/UserNotFoundException.java`:

```java
package com.axiserp.auth.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

Add to GlobalExceptionHandler:

```java
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }
```

**Step 2: Modify CreateUserUseCaseImpl**

Add after `createdBy` validation — look up admin and verify active:

```java
    private final UserRepositoryPort userRepositoryPort;
```
(already injected)

After `if (userRepositoryPort.existsByEmail(...))` block, add:

```java
        User admin = userRepositoryPort.findById(createdBy)
                .orElseThrow(() -> new RuntimeException("Usuario administrador no encontrado"));
        if (!admin.isActive()) {
            throw new UserInactiveException(
                    "El usuario administrador no puede crear usuarios porque está " +
                    admin.getStatus().name().toLowerCase());
        }
```

**Step 3: Modify UpdateUserUseCaseImpl**

After `User user = userRepositoryPort.findById(id)` block, add:

```java
        if (user.getStatus() == User.UserStatus.ELIMINADO) {
            throw new com.axiserp.auth.domain.exception.UserNotFoundException(
                    "Usuario no encontrado");
        }
```

**Step 4: Modify DeactivateUserUseCaseImpl**

After existing INACTIVO check, add:

```java
        if (user.getStatus() == User.UserStatus.ELIMINADO) {
            throw new IllegalStateException("El usuario ya está eliminado");
        }
```

**Step 5: Modify GetUserUseCaseImpl**

After `User user = userRepositoryPort.findById(id)`, add:

```java
        if (user.getStatus() == User.UserStatus.ELIMINADO) {
            throw new com.axiserp.auth.domain.exception.UserNotFoundException(
                    "Usuario no encontrado");
        }
```

**Step 6: Update CreateUserUseCaseImplTest**

Add a test for admin inactive scenario after the existing tests:

```java
    @Test
    @DisplayName("Should throw UserInactiveException when admin is inactive")
    void create_adminInactive() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "ADMIN");

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(userRepositoryPort.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).status(User.UserStatus.INACTIVO).build()));

        assertThrows(UserInactiveException.class, () -> createUserUseCase.create(request, adminId));
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }
```

Add import:
```java
import com.axiserp.auth.domain.exception.UserInactiveException;
```

**Step 7: Update DeactivateUserUseCaseImplTest**

Add test for already deleted user:
```java
    @Test
    @DisplayName("Should throw IllegalStateException when user is already ELIMINADO")
    void deactivate_alreadyDeleted() {
        User deletedUser = User.builder()
                .id(UUID.randomUUID())
                .status(User.UserStatus.ELIMINADO)
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        when(userRepositoryPort.findById(deletedUser.getId())).thenReturn(Optional.of(deletedUser));

        assertThrows(IllegalStateException.class,
                () -> deactivateUserUseCase.deactivate(deletedUser.getId()));
    }
```

**Step 8: Run all tests**

```bash
cd auth-service && .\mvnw.cmd test -q
```
Expected: All tests pass

---

### Task 5: Add password reset endpoint (delegate to Supabase)

**Files:**
- Modify: `ports/output/SupabaseAuthPort.java`
- Create: `application/dto/request/PasswordResetRequest.java`
- Modify: `infrastructure/adapters/out/supabase/SupabaseAdminAdapter.java`
- Modify: `infrastructure/adapters/in/web/controller/AuthController.java`

**Step 1: Add sendPasswordReset to port**

Update `SupabaseAuthPort.java`:

```java
public interface SupabaseAuthPort {
    SupabaseUser createUser(String email, String roleName, String name, UUID createdBy);
    void sendPasswordReset(String email);
}
```

**Step 2: Create PasswordResetRequest DTO**

Create `src/main/java/com/axiserp/auth/application/dto/request/PasswordResetRequest.java`:

```java
package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
    @NotBlank @Email String email
) {}
```

**Step 3: Implement sendPasswordReset in SupabaseAdminAdapter**

Add this method:

```java
    @Override
    public void sendPasswordReset(String email) {
        log.info("Sending password reset email to: {}", email);

        try {
            restClient.post()
                    .uri("https://" + extractProjectRef() + ".supabase.co/auth/v1/recover")
                    .body(Map.of("email", email))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            log.warn("Supabase password reset API responded with: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Swallow — never reveal if email exists or not (OWASP A07)
        }
    }

    private String extractProjectRef() {
        return "hbtcusxbkkefphunarwn";
    }
```

Wait — better approach: use a separate RestClient for the public GoTrue recover endpoint (uses anon key, not service role key). Create a second RestClient:

```java
    private final RestClient publicRestClient;

    public SupabaseAdminAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey,
            @Value("${supabase.anon-key}") String anonKey) {

        String baseUrl = supabaseUrl + "/auth/v1/admin";

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        this.publicRestClient = restClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", anonKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
```

Add `supabase.anon-key` property to `application.properties`:
```properties
supabase.anon-key=${SUPABASE_ANON_KEY}
```

Implementation:
```java
    @Override
    public void sendPasswordReset(String email) {
        log.info("Sending password reset email to: {}", email);
        try {
            publicRestClient.post()
                    .uri("/recover")
                    .body(Map.of("email", email))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            log.warn("Password reset API responded: {}", e.getStatusCode());
        }
    }
```

**Step 4: Add endpoint to AuthController**

Add new controller method:

```java
    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, Object>> passwordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        supabaseAuthPort.sendPasswordReset(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "Si el correo existe, recibirás un enlace de recuperación"));
    }
```

Add imports:
```java
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.axiserp.auth.application.dto.request.PasswordResetRequest;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
```

Inject `SupabaseAuthPort`:
```java
    private final SupabaseAuthPort supabaseAuthPort;
```

**Step 5: Verify compilation**

```bash
cd auth-service && .\mvnw.cmd clean compile -q
```
Expected: no output

---

### Task 6: Rate limiting with Bucket4j

**Files:**
- Create: `infrastructure/config/RateLimitingFilter.java`
- Modify: `infrastructure/config/SecurityConfig.java`
- Modify: `infrastructure/adapters/in/web/exception/GlobalExceptionHandler.java`

**Step 1: Create RateLimitingFilter**

Create `src/main/java/com/axiserp/auth/infrastructure/config/RateLimitingFilter.java`:

```java
package com.axiserp.auth.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        Bucket bucket = ipBuckets.get(clientIp, k -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 429);
            body.put("error", "Too Many Requests");
            body.put("message", "Demasiadas solicitudes. Intente nuevamente en 60 segundos.");
            body.put("timestamp", java.time.Instant.now().toString());

            try (var writer = response.getWriter()) {
                new com.fasterxml.jackson.databind.ObjectMapper().writeValue(writer, body);
            }
        }
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
```

**Step 2: Register in SecurityConfig**

Add field:
```java
    private final RateLimitingFilter rateLimitingFilter;
```

Add before `firstLoginFilter` in the chain:
```java
                .addFilterBefore(rateLimitingFilter, FirstLoginFilter.class)
```

**Step 3: Verify compilation**

```bash
cd auth-service && .\mvnw.cmd clean compile -q
```
Expected: no output

---

### Task 7: Run full test suite and build

```bash
cd auth-service && .\mvnw.cmd clean test
```
Expected: BUILD SUCCESS, all tests pass.
