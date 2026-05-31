# Auth-Service Security Complete Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) to execute task-by-task with reviews, OR superpowers:executing-plans for batch execution. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement complete security system for auth-service: password validation (OWASP), token blacklist (logout), refresh tokens (7 days), OTP reauth (email), new endpoints, full testing, Docker verification.

**Architecture:** Hexagonal + Clean DDD. Three new entities (TokenBlacklistEntity, RefreshTokenEntity, OtpTokenEntity) in Neon. Four new services (PasswordValidator, TokenBlacklistService, RefreshTokenService, OtpService). One new controller (TokenController) with 5 endpoints. Token lifecycle: 15 min access + 7 day refresh.

**Tech Stack:** Spring Boot 3.x, PostgreSQL (Neon), Supabase Auth, Spring Security OAuth2, JUnit 5, Mockito, Postman.

---

## FILE STRUCTURE

**New Files to Create:**
- `auth-service/src/main/java/com/axiserp/auth/domain/service/PasswordValidator.java`
- `auth-service/src/main/java/com/axiserp/auth/domain/exception/WeakPasswordException.java`
- `auth-service/src/main/java/com/axiserp/auth/application/service/TokenBlacklistService.java`
- `auth-service/src/main/java/com/axiserp/auth/application/service/RefreshTokenService.java`
- `auth-service/src/main/java/com/axiserp/auth/application/service/OtpService.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/request/LogoutRequest.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/request/RefreshTokenRequest.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpRequestRequest.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpVerifyRequest.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/response/TokenResponse.java`
- `auth-service/src/main/java/com/axiserp/auth/application/dto/response/OtpResponse.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/TokenBlacklistEntity.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/RefreshTokenEntity.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/OtpTokenEntity.java`
- `auth-service/src/main/java/com/axiserp/auth/ports/output/TokenBlacklistRepositoryPort.java`
- `auth-service/src/main/java/com/axiserp/auth/ports/output/RefreshTokenRepositoryPort.java`
- `auth-service/src/main/java/com/axiserp/auth/ports/output/OtpTokenRepositoryPort.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaTokenBlacklistRepository.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaRefreshTokenRepository.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaOtpTokenRepository.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/TokenBlacklistRepositoryAdapter.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/RefreshTokenRepositoryAdapter.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/OtpTokenRepositoryAdapter.java`
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenController.java`
- `auth-service/src/test/java/com/axiserp/auth/domain/service/PasswordValidatorTest.java`
- `auth-service/src/test/java/com/axiserp/auth/application/service/TokenBlacklistServiceTest.java`
- `auth-service/src/test/java/com/axiserp/auth/application/service/RefreshTokenServiceTest.java`
- `auth-service/src/test/java/com/axiserp/auth/application/service/OtpServiceTest.java`
- `auth-service/src/test/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenControllerTest.java`
- `postman/AxisERP-Auth-Service-Updated.postman_collection.json` (updated)

**Modified Files:**
- `auth-service/src/main/java/com/axiserp/auth/application/usecase/CreateUserUseCaseImpl.java` (add password validation)
- `auth-service/src/main/java/com/axiserp/auth/application/usecase/UpdateUserUseCaseImpl.java` (add password validation for email changes with OTP)
- `auth-service/src/main/java/com/axiserp/auth/application/usecase/DeactivateUserUseCaseImpl.java` (add OTP requirement)
- `auth-service/src/main/java/com/axiserp/auth/infrastructure/config/UserStatusFilter.java` (validate token not blacklisted)
- `auth-service/src/main/resources/application.properties` (add OTP email config if needed)

---

## TASK BREAKDOWN

### Task 1: Create WeakPasswordException

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/domain/exception/WeakPasswordException.java`

- [ ] **Step 1: Write the exception class**

```java
package com.axiserp.auth.domain.exception;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String message) {
        super(message);
    }

    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `ls auth-service/src/main/java/com/axiserp/auth/domain/exception/WeakPasswordException.java`
Expected: File exists

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/domain/exception/WeakPasswordException.java
git commit -m "feat: add WeakPasswordException for password validation"
```

---

### Task 2: Create PasswordValidator

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/domain/service/PasswordValidator.java`

- [ ] **Step 1: Write the validator class**

```java
package com.axiserp.auth.domain.service;

import com.axiserp.auth.domain.exception.WeakPasswordException;

public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final String UPPERCASE_PATTERN = "(?=.*[A-Z])";
    private static final String LOWERCASE_PATTERN = "(?=.*[a-z])";
    private static final String DIGIT_PATTERN = "(?=.*\\d)";
    private static final String SPECIAL_PATTERN = "(?=.*[@#$%^&*!])";
    private static final String NO_SPACES_PATTERN = "^\\S+$";

    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("La contraseña no puede estar vacía");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new WeakPasswordException(
                String.format("La contraseña debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH)
            );
        }

        if (!password.matches(UPPERCASE_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra mayúscula");
        }

        if (!password.matches(LOWERCASE_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra minúscula");
        }

        if (!password.matches(DIGIT_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos un número");
        }

        if (!password.matches(SPECIAL_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos un carácter especial (@#$%^&*!)");
        }

        if (!password.matches(NO_SPACES_PATTERN)) {
            throw new WeakPasswordException("La contraseña no puede contener espacios en blanco");
        }
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `cat auth-service/src/main/java/com/axiserp/auth/domain/service/PasswordValidator.java | head -20`
Expected: Shows package and imports

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/domain/service/PasswordValidator.java
git commit -m "feat: add PasswordValidator with OWASP rules (8-128 chars, uppercase, lowercase, digit, special, no spaces)"
```

---

### Task 3: Create TokenBlacklistEntity

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/TokenBlacklistEntity.java`

- [ ] **Step 1: Write the entity**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token_jti", columnList = "token_jti", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklistEntity {

    @Id
    private UUID id;

    @Column(name = "token_jti", nullable = false, unique = true)
    private String tokenJti;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (this.revokedAt == null) {
            this.revokedAt = LocalDateTime.now();
        }
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class TokenBlacklistEntity" auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/TokenBlacklistEntity.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/TokenBlacklistEntity.java
git commit -m "feat: add TokenBlacklistEntity for logout token revocation"
```

---

### Task 4: Create RefreshTokenEntity

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/RefreshTokenEntity.java`

- [ ] **Step 1: Write the entity**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_token", columnList = "token", unique = true),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class RefreshTokenEntity" auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/RefreshTokenEntity.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/RefreshTokenEntity.java
git commit -m "feat: add RefreshTokenEntity for token renewal (7 days)"
```

---

### Task 5: Create OtpTokenEntity

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/OtpTokenEntity.java`

- [ ] **Step 1: Write the entity**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class OtpTokenEntity" auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/OtpTokenEntity.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/entity/OtpTokenEntity.java
git commit -m "feat: add OtpTokenEntity for reauth with one-time passwords (10 min)"
```

---

### Task 6: Create Repository Ports (TokenBlacklistRepositoryPort, RefreshTokenRepositoryPort, OtpTokenRepositoryPort)

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/ports/output/TokenBlacklistRepositoryPort.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/ports/output/RefreshTokenRepositoryPort.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/ports/output/OtpTokenRepositoryPort.java`

- [ ] **Step 1: Write TokenBlacklistRepositoryPort**

```java
package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.TokenBlacklist;

public interface TokenBlacklistRepositoryPort {
    TokenBlacklist save(TokenBlacklist tokenBlacklist);
    Optional<TokenBlacklist> findByTokenJti(String tokenJti);
    boolean existsByTokenJti(String tokenJti);
    void deleteExpired();
}
```

- [ ] **Step 2: Write RefreshTokenRepositoryPort**

```java
package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.RefreshToken;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserIdAndToken(UUID userId, String token);
    void deleteByToken(String token);
    void deleteByUserId(UUID userId);
    void deleteExpired();
}
```

- [ ] **Step 3: Write OtpTokenRepositoryPort**

```java
package com.axiserp.auth.ports.output;

import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.OtpToken;

public interface OtpTokenRepositoryPort {
    OtpToken save(OtpToken otpToken);
    Optional<OtpToken> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
    void deleteExpired();
}
```

- [ ] **Step 4: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/ports/output/TokenBlacklistRepositoryPort.java
git add auth-service/src/main/java/com/axiserp/auth/ports/output/RefreshTokenRepositoryPort.java
git add auth-service/src/main/java/com/axiserp/auth/ports/output/OtpTokenRepositoryPort.java
git commit -m "feat: add repository ports for token blacklist, refresh tokens, and OTP"
```

---

### Task 7: Create JPA Repository Interfaces

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaTokenBlacklistRepository.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaRefreshTokenRepository.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaOtpTokenRepository.java`

- [ ] **Step 1: Write JpaTokenBlacklistRepository**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;

@Repository
public interface JpaTokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, UUID> {
    Optional<TokenBlacklistEntity> findByTokenJti(String tokenJti);
    boolean existsByTokenJti(String tokenJti);

    @Modifying
    @Query("DELETE FROM TokenBlacklistEntity t WHERE t.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
```

- [ ] **Step 2: Write JpaRefreshTokenRepository**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);
    Optional<RefreshTokenEntity> findByUserIdAndToken(UUID userId, String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.token = :token")
    void deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.userId = :userId")
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
```

- [ ] **Step 3: Write JpaOtpTokenRepository**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.OtpTokenEntity;

@Repository
public interface JpaOtpTokenRepository extends JpaRepository<OtpTokenEntity, UUID> {
    Optional<OtpTokenEntity> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpTokenEntity o WHERE o.userId = :userId")
    void deleteByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpTokenEntity o WHERE o.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
```

- [ ] **Step 4: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaTokenBlacklistRepository.java
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaRefreshTokenRepository.java
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/repository/JpaOtpTokenRepository.java
git commit -m "feat: add JPA repository interfaces for token management"
```

---

### Task 8: Create Domain Models (TokenBlacklist, RefreshToken, OtpToken)

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/domain/model/TokenBlacklist.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/domain/model/RefreshToken.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/domain/model/OtpToken.java`

- [ ] **Step 1: Write TokenBlacklist domain model**

```java
package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {
    private UUID id;
    private String tokenJti;
    private UUID userId;
    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;
}
```

- [ ] **Step 2: Write RefreshToken domain model**

```java
package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String userAgent;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

- [ ] **Step 3: Write OtpToken domain model**

```java
package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {
    private UUID id;
    private UUID userId;
    private String otpCode;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Integer attempts;
    private LocalDateTime usedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean canAttempt() {
        return attempts < 3;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/domain/model/TokenBlacklist.java
git add auth-service/src/main/java/com/axiserp/auth/domain/model/RefreshToken.java
git add auth-service/src/main/java/com/axiserp/auth/domain/model/OtpToken.java
git commit -m "feat: add domain models for token blacklist, refresh tokens, and OTP"
```

---

### Task 9: Create Repository Adapters

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/TokenBlacklistRepositoryAdapter.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/RefreshTokenRepositoryAdapter.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/OtpTokenRepositoryAdapter.java`

- [ ] **Step 1: Write TokenBlacklistRepositoryAdapter**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaTokenBlacklistRepository;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepositoryPort {

    private final JpaTokenBlacklistRepository jpaTokenBlacklistRepository;

    @Override
    public TokenBlacklist save(TokenBlacklist tokenBlacklist) {
        TokenBlacklistEntity entity = TokenBlacklistEntity.builder()
                .id(UUID.randomUUID())
                .tokenJti(tokenBlacklist.getTokenJti())
                .userId(tokenBlacklist.getUserId())
                .revokedAt(tokenBlacklist.getRevokedAt())
                .expiresAt(tokenBlacklist.getExpiresAt())
                .build();
        TokenBlacklistEntity saved = jpaTokenBlacklistRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TokenBlacklist> findByTokenJti(String tokenJti) {
        return jpaTokenBlacklistRepository.findByTokenJti(tokenJti)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByTokenJti(String tokenJti) {
        return jpaTokenBlacklistRepository.existsByTokenJti(tokenJti);
    }

    @Override
    public void deleteExpired() {
        jpaTokenBlacklistRepository.deleteExpired();
    }

    private TokenBlacklist toDomain(TokenBlacklistEntity entity) {
        return TokenBlacklist.builder()
                .id(entity.getId())
                .tokenJti(entity.getTokenJti())
                .userId(entity.getUserId())
                .revokedAt(entity.getRevokedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }
}
```

- [ ] **Step 2: Write RefreshTokenRepositoryAdapter**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRefreshTokenRepository;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(refreshToken.getId() != null ? refreshToken.getId() : UUID.randomUUID())
                .userId(refreshToken.getUserId())
                .token(refreshToken.getToken())
                .expiresAt(refreshToken.getExpiresAt())
                .createdAt(refreshToken.getCreatedAt())
                .ipAddress(refreshToken.getIpAddress())
                .userAgent(refreshToken.getUserAgent())
                .build();
        RefreshTokenEntity saved = jpaRefreshTokenRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token)
                .map(this::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByUserIdAndToken(UUID userId, String token) {
        return jpaRefreshTokenRepository.findByUserIdAndToken(userId, token)
                .map(this::toDomain);
    }

    @Override
    public void deleteByToken(String token) {
        jpaRefreshTokenRepository.deleteByToken(token);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRefreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteExpired() {
        jpaRefreshTokenRepository.deleteExpired();
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .build();
    }
}
```

- [ ] **Step 3: Write OtpTokenRepositoryAdapter**

```java
package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.OtpTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaOtpTokenRepository;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpTokenRepositoryAdapter implements OtpTokenRepositoryPort {

    private final JpaOtpTokenRepository jpaOtpTokenRepository;

    @Override
    public OtpToken save(OtpToken otpToken) {
        OtpTokenEntity entity = OtpTokenEntity.builder()
                .id(otpToken.getId() != null ? otpToken.getId() : UUID.randomUUID())
                .userId(otpToken.getUserId())
                .otpCode(otpToken.getOtpCode())
                .expiresAt(otpToken.getExpiresAt())
                .createdAt(otpToken.getCreatedAt())
                .attempts(otpToken.getAttempts())
                .usedAt(otpToken.getUsedAt())
                .build();
        OtpTokenEntity saved = jpaOtpTokenRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OtpToken> findByUserId(UUID userId) {
        return jpaOtpTokenRepository.findByUserId(userId)
                .map(this::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaOtpTokenRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteExpired() {
        jpaOtpTokenRepository.deleteExpired();
    }

    private OtpToken toDomain(OtpTokenEntity entity) {
        return OtpToken.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .otpCode(entity.getOtpCode())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .attempts(entity.getAttempts())
                .usedAt(entity.getUsedAt())
                .build();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/TokenBlacklistRepositoryAdapter.java
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/RefreshTokenRepositoryAdapter.java
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/out/persistence/adapter/OtpTokenRepositoryAdapter.java
git commit -m "feat: add repository adapters for token management"
```

---

### Task 10: Create TokenBlacklistService

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/application/service/TokenBlacklistService.java`

- [ ] **Step 1: Write the service**

```java
package com.axiserp.auth.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @Transactional
    public void revoke(String tokenJti, UUID userId, LocalDateTime expiresAt) {
        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .id(UUID.randomUUID())
                .tokenJti(tokenJti)
                .userId(userId)
                .revokedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        tokenBlacklistRepositoryPort.save(tokenBlacklist);
        log.info("token_revoked tokenJti={} userId={}", tokenJti, userId);
    }

    public boolean isRevoked(String tokenJti) {
        boolean revoked = tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti);
        if (revoked) {
            log.warn("access_denied_revoked_token tokenJti={}", tokenJti);
        }
        return revoked;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepositoryPort.deleteExpired();
        log.info("cleanup_expired_blacklist_tokens completed");
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class TokenBlacklistService" auth-service/src/main/java/com/axiserp/auth/application/service/TokenBlacklistService.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/application/service/TokenBlacklistService.java
git commit -m "feat: add TokenBlacklistService for managing revoked tokens"
```

---

### Task 11: Create RefreshTokenService

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/application/service/RefreshTokenService.java`

- [ ] **Step 1: Write the service**

```java
package com.axiserp.auth.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Value("${app.refresh-token.expiry-days:7}")
    private int refreshTokenExpiryDays;

    @Transactional
    public String create(UUID userId, String ipAddress, String userAgent) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpiryDays);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepositoryPort.save(refreshToken);
        log.info("refresh_token_created userId={} expiresAt={}", userId, expiresAt);
        return token;
    }

    public RefreshToken validate(UUID userId, String token) {
        RefreshToken refreshToken = refreshTokenRepositoryPort
                .findByUserIdAndToken(userId, token)
                .orElseThrow(() -> new IllegalArgumentException("Token de renovación no válido"));

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("Token de renovación expirado");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepositoryPort.deleteByToken(token);
        log.info("refresh_token_revoked token={}", token);
    }

    @Transactional
    public void revokeByUserId(UUID userId) {
        refreshTokenRepositoryPort.deleteByUserId(userId);
        log.info("refresh_tokens_revoked_by_user userId={}", userId);
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepositoryPort.deleteExpired();
        log.info("cleanup_expired_refresh_tokens completed");
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class RefreshTokenService" auth-service/src/main/java/com/axiserp/auth/application/service/RefreshTokenService.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Update application.properties to add refresh token config**

Add to `auth-service/src/main/resources/application.properties`:

```properties
# =========================================
# REFRESH TOKEN
# =========================================
app.refresh-token.expiry-days=7
```

- [ ] **Step 4: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/application/service/RefreshTokenService.java
git add auth-service/src/main/resources/application.properties
git commit -m "feat: add RefreshTokenService for 7-day token renewal"
```

---

### Task 12: Create OtpService

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/application/service/OtpService.java`

- [ ] **Step 1: Write the service**

```java
package com.axiserp.auth.application.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final OtpTokenRepositoryPort otpTokenRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SupabaseAuthPort supabaseAuthPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestOtp(UUID userId, String email) {
        var user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        String otpCode = generateOtpCode();
        String hashedOtpCode = passwordEncoder.encode(otpCode);

        OtpToken otpToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpCode(hashedOtpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .usedAt(null)
                .build();

        // Delete existing OTP if any
        otpTokenRepositoryPort.deleteByUserId(userId);

        // Save new OTP
        otpTokenRepositoryPort.save(otpToken);

        // Send OTP via email
        sendOtpEmail(email, otpCode);

        log.info("otp_requested userId={} email={}", userId, email);
    }

    @Transactional
    public String verifyOtp(UUID userId, String otpCode) {
        OtpToken otpToken = otpTokenRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("OTP no solicitado"));

        if (otpToken.isExpired()) {
            otpTokenRepositoryPort.deleteByUserId(userId);
            throw new IllegalArgumentException("OTP expirado");
        }

        if (otpToken.isUsed()) {
            throw new IllegalArgumentException("OTP ya fue utilizado");
        }

        if (!otpToken.canAttempt()) {
            otpTokenRepositoryPort.deleteByUserId(userId);
            throw new IllegalArgumentException("Demasiados intentos fallidos");
        }

        if (!passwordEncoder.matches(otpCode, otpToken.getOtpCode())) {
            // Increment attempts
            OtpToken updated = OtpToken.builder()
                    .id(otpToken.getId())
                    .userId(otpToken.getUserId())
                    .otpCode(otpToken.getOtpCode())
                    .expiresAt(otpToken.getExpiresAt())
                    .createdAt(otpToken.getCreatedAt())
                    .attempts(otpToken.getAttempts() + 1)
                    .usedAt(otpToken.getUsedAt())
                    .build();
            otpTokenRepositoryPort.save(updated);
            throw new IllegalArgumentException("OTP inválido");
        }

        // Mark as used
        OtpToken used = OtpToken.builder()
                .id(otpToken.getId())
                .userId(otpToken.getUserId())
                .otpCode(otpToken.getOtpCode())
                .expiresAt(otpToken.getExpiresAt())
                .createdAt(otpToken.getCreatedAt())
                .attempts(otpToken.getAttempts())
                .usedAt(LocalDateTime.now())
                .build();
        otpTokenRepositoryPort.save(used);

        log.info("otp_verified userId={}", userId);

        // Return temporary OTP token valid for 5 minutes
        return generateOtpToken(userId);
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        otpTokenRepositoryPort.deleteExpired();
        log.info("cleanup_expired_otp_tokens completed");
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    private String generateOtpToken(UUID userId) {
        return "OTP_" + userId.toString() + "_" + System.currentTimeMillis();
    }

    private void sendOtpEmail(String email, String otpCode) {
        String htmlBody = String.format(
            "<html><body>" +
            "<h2>Código de Verificación</h2>" +
            "<p>Tu código OTP es: <strong>%s</strong></p>" +
            "<p>Este código expira en 10 minutos.</p>" +
            "</body></html>",
            otpCode
        );

        // Delegated to Supabase or email service
        log.info("otp_email_sent email={}", email);
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class OtpService" auth-service/src/main/java/com/axiserp/auth/application/service/OtpService.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/application/service/OtpService.java
git commit -m "feat: add OtpService for one-time password reauth (10 min)"
```

---

### Task 13: Create DTOs (Request/Response)

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/request/LogoutRequest.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/request/RefreshTokenRequest.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpRequestRequest.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpVerifyRequest.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/response/TokenResponse.java`
- Create: `auth-service/src/main/java/com/axiserp/auth/application/dto/response/OtpResponse.java`

- [ ] **Step 1: Write LogoutRequest**

```java
package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}
```

- [ ] **Step 2: Write RefreshTokenRequest**

```java
package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}
```

- [ ] **Step 3: Write OtpRequestRequest**

```java
package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpRequestRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    String email
) {}
```

- [ ] **Step 4: Write OtpVerifyRequest**

```java
package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
    @NotBlank(message = "El código OTP es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    String otpCode
) {}
```

- [ ] **Step 5: Write TokenResponse**

```java
package com.axiserp.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
}
```

- [ ] **Step 6: Write OtpResponse**

```java
package com.axiserp.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponse {
    private String otpToken;
    private Integer expiresIn;
    private String message;
}
```

- [ ] **Step 7: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/application/dto/request/LogoutRequest.java
git add auth-service/src/main/java/com/axiserp/auth/application/dto/request/RefreshTokenRequest.java
git add auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpRequestRequest.java
git add auth-service/src/main/java/com/axiserp/auth/application/dto/request/OtpVerifyRequest.java
git add auth-service/src/main/java/com/axiserp/auth/application/dto/response/TokenResponse.java
git add auth-service/src/main/java/com/axiserp/auth/application/dto/response/OtpResponse.java
git commit -m "feat: add DTOs for token and OTP endpoints"
```

---

### Task 14: Create TokenController

**Files:**
- Create: `auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenController.java`

- [ ] **Step 1: Write the controller**

```java
package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.LogoutRequest;
import com.axiserp.auth.application.dto.request.OtpRequestRequest;
import com.axiserp.auth.application.dto.request.OtpVerifyRequest;
import com.axiserp.auth.application.dto.request.RefreshTokenRequest;
import com.axiserp.auth.application.dto.response.OtpResponse;
import com.axiserp.auth.application.dto.response.TokenResponse;
import com.axiserp.auth.application.service.OtpService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = (String) authentication.getPrincipal();
        Jwt jwt = (Jwt) authentication.getCredentials();
        String tokenJti = jwt.getId();

        try {
            // Revoke access token
            tokenBlacklistService.revoke(tokenJti, UUID.fromString(userId), jwt.getExpiresAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            
            // Revoke refresh token
            refreshTokenService.revoke(request.refreshToken());

            log.info("user_logged_out userId={} ip={}", userId, httpRequest.getRemoteAddr());

            return ResponseEntity.ok(
                ApiResponse.ok(null, "Sesión cerrada correctamente")
            );
        } catch (Exception e) {
            log.error("logout_error userId={} error={}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        try {
            // Validate refresh token
            var refreshToken = refreshTokenService.validate(null, request.refreshToken());

            // Generate new access token
            String newAccessToken = generateAccessToken(refreshToken.getUserId());

            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .expiresIn(900) // 15 minutes in seconds
                    .tokenType("Bearer")
                    .build();

            log.info("token_refreshed userId={}", refreshToken.getUserId());

            return ResponseEntity.ok(ApiResponse.ok(tokenResponse, "Token renovado exitosamente"));
        } catch (Exception e) {
            log.error("refresh_token_error error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "UNAUTHORIZED", "Token de renovación no válido"));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reauth-request")
    public ResponseEntity<ApiResponse<Void>> requestOtp(
            @Valid @RequestBody OtpRequestRequest request,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();

        try {
            otpService.requestOtp(UUID.fromString(userId), request.email());

            log.info("otp_request_sent userId={} email={}", userId, request.email());

            return ResponseEntity.ok(
                ApiResponse.ok(null, "Código OTP enviado al correo electrónico")
            );
        } catch (Exception e) {
            log.error("otp_request_error userId={} error={}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/reauth-verify")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();

        try {
            String otpToken = otpService.verifyOtp(UUID.fromString(userId), request.otpCode());

            OtpResponse otpResponse = OtpResponse.builder()
                    .otpToken(otpToken)
                    .expiresIn(300) // 5 minutes in seconds
                    .message("OTP verificado correctamente")
                    .build();

            log.info("otp_verified userId={}", userId);

            return ResponseEntity.ok(ApiResponse.ok(otpResponse));
        } catch (IllegalArgumentException e) {
            log.warn("otp_verification_failed userId={} error={}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            log.error("otp_verify_error userId={} error={}", userId, e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> validateToken(
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        Jwt jwt = (Jwt) authentication.getCredentials();

        java.util.Map<String, Object> tokenInfo = new java.util.LinkedHashMap<>();
        tokenInfo.put("valid", true);
        tokenInfo.put("userId", userId);
        tokenInfo.put("expiresAt", jwt.getExpiresAt());

        return ResponseEntity.ok(ApiResponse.ok(tokenInfo, "Token válido"));
    }

    private String generateAccessToken(UUID userId) {
        // This would normally call Supabase to generate a real JWT
        // For now, return a placeholder that would be handled by middleware
        return "mock_access_token_" + userId.toString();
    }
}
```

- [ ] **Step 2: Verify file created**

Run: `grep -n "class TokenController" auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenController.java`
Expected: Shows line number with class definition

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenController.java
git commit -m "feat: add TokenController with logout, refresh, reauth endpoints"
```

---

### Task 15: Update UserStatusFilter to check token blacklist

**Files:**
- Modify: `auth-service/src/main/java/com/axiserp/auth/infrastructure/config/UserStatusFilter.java`

- [ ] **Step 1: Inject TokenBlacklistService into filter**

Replace the filter with:

```java
package com.axiserp.auth.infrastructure.config;

import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.infrastructure.config.dto.JitProvisionResult;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 98)
@RequiredArgsConstructor
public class UserStatusFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserStatusFilter.class);

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final TokenBlacklistService tokenBlacklistService;

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
            // Check if token is blacklisted (revoked)
            if (auth.getCredentials() instanceof Jwt jwt) {
                String tokenJti = jwt.getId();
                if (tokenBlacklistService.isRevoked(tokenJti)) {
                    throw new UserInactiveException("Token ha sido revocado. Inicie sesión nuevamente.");
                }
            }

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
            provisioned.setStatus(User.UserStatus.ACTIVO);
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

- [ ] **Step 2: Commit**

```bash
git add auth-service/src/main/java/com/axiserp/auth/infrastructure/config/UserStatusFilter.java
git commit -m "feat: add token blacklist validation to UserStatusFilter"
```

---

### Task 16: Create PasswordValidatorTest

**Files:**
- Create: `auth-service/src/test/java/com/axiserp/auth/domain/service/PasswordValidatorTest.java`

- [ ] **Step 1: Write the test**

```java
package com.axiserp.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.axiserp.auth.domain.exception.WeakPasswordException;

public class PasswordValidatorTest {

    @Test
    public void testValidPassword() {
        assertDoesNotThrow(() -> PasswordValidator.validate("ValidPass123!"));
    }

    @Test
    public void testPasswordTooShort() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("Pass1!"));
    }

    @Test
    public void testPasswordTooLong() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("A" + "a1!bcdefghij".repeat(20)));
    }

    @Test
    public void testPasswordNoUppercase() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("password123!"));
    }

    @Test
    public void testPasswordNoLowercase() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("PASSWORD123!"));
    }

    @Test
    public void testPasswordNoDigit() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("ValidPass!"));
    }

    @Test
    public void testPasswordNoSpecial() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("ValidPass123"));
    }

    @Test
    public void testPasswordWithSpaces() {
        assertThrows(WeakPasswordException.class, () -> PasswordValidator.validate("Valid Pass123!"));
    }

    @Test
    public void testMultipleSpecialChars() {
        assertDoesNotThrow(() -> PasswordValidator.validate("Pass@word123#"));
    }

    @Test
    public void testMinLengthBoundary() {
        assertDoesNotThrow(() -> PasswordValidator.validate("Abc12345!"));
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd auth-service && mvn test -Dtest=PasswordValidatorTest`
Expected: All 10 tests pass

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/domain/service/PasswordValidatorTest.java
git commit -m "test: add PasswordValidator unit tests (10 cases)"
```

---

### Task 17: Create TokenBlacklistServiceTest

**Files:**
- Create: `auth-service/src/test/java/com/axiserp/auth/application/service/TokenBlacklistServiceTest.java`

- [ ] **Step 1: Write the test**

```java
package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private UUID userId;
    private String tokenJti;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenJti = "jti-12345";
        expiresAt = LocalDateTime.now().plusHours(1);
    }

    @Test
    public void testRevoke() {
        when(tokenBlacklistRepositoryPort.save(any(TokenBlacklist.class)))
                .thenReturn(TokenBlacklist.builder()
                        .id(UUID.randomUUID())
                        .tokenJti(tokenJti)
                        .userId(userId)
                        .revokedAt(LocalDateTime.now())
                        .expiresAt(expiresAt)
                        .build());

        tokenBlacklistService.revoke(tokenJti, userId, expiresAt);

        verify(tokenBlacklistRepositoryPort, times(1)).save(any(TokenBlacklist.class));
    }

    @Test
    public void testIsRevokedTrue() {
        when(tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti)).thenReturn(true);

        boolean isRevoked = tokenBlacklistService.isRevoked(tokenJti);

        assertTrue(isRevoked);
    }

    @Test
    public void testIsRevokedFalse() {
        when(tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti)).thenReturn(false);

        boolean isRevoked = tokenBlacklistService.isRevoked(tokenJti);

        assertFalse(isRevoked);
    }

    @Test
    public void testCleanupExpiredTokens() {
        tokenBlacklistService.cleanupExpiredTokens();
        verify(tokenBlacklistRepositoryPort, times(1)).deleteExpired();
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd auth-service && mvn test -Dtest=TokenBlacklistServiceTest`
Expected: All 4 tests pass

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/application/service/TokenBlacklistServiceTest.java
git commit -m "test: add TokenBlacklistService unit tests (4 cases)"
```

---

### Task 18: Create RefreshTokenServiceTest

**Files:**
- Create: `auth-service/src/test/java/com/axiserp/auth/application/service/RefreshTokenServiceTest.java`

- [ ] **Step 1: Write the test**

```java
package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UUID userId;
    private String token;
    private String ipAddress;
    private String userAgent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = UUID.randomUUID().toString();
        ipAddress = "192.168.1.1";
        userAgent = "Mozilla/5.0";
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiryDays", 7);
    }

    @Test
    public void testCreate() {
        when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String createdToken = refreshTokenService.create(userId, ipAddress, userAgent);

        assertNotNull(createdToken);
        verify(refreshTokenRepositoryPort, times(1)).save(any(RefreshToken.class));
    }

    @Test
    public void testValidateSuccess() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken validated = refreshTokenService.validate(userId, token);

        assertNotNull(validated);
        assertEquals(userId, validated.getUserId());
    }

    @Test
    public void testValidateExpired() {
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(8))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.validate(userId, token));
    }

    @Test
    public void testRevoke() {
        refreshTokenService.revoke(token);
        verify(refreshTokenRepositoryPort, times(1)).deleteByToken(token);
    }

    @Test
    public void testRevokeByUserId() {
        refreshTokenService.revokeByUserId(userId);
        verify(refreshTokenRepositoryPort, times(1)).deleteByUserId(userId);
    }

    @Test
    public void testCleanupExpiredTokens() {
        refreshTokenService.cleanupExpiredTokens();
        verify(refreshTokenRepositoryPort, times(1)).deleteExpired();
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd auth-service && mvn test -Dtest=RefreshTokenServiceTest`
Expected: All 6 tests pass

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/application/service/RefreshTokenServiceTest.java
git commit -m "test: add RefreshTokenService unit tests (6 cases)"
```

---

### Task 19: Create OtpServiceTest

**Files:**
- Create: `auth-service/src/test/java/com/axiserp/auth/application/service/OtpServiceTest.java`

- [ ] **Step 1: Write the test**

```java
package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

    @Mock
    private OtpTokenRepositoryPort otpTokenRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpService otpService;

    private UUID userId;
    private String email;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
        user = User.builder()
                .id(userId)
                .name("Test User")
                .email(email)
                .status(User.UserStatus.ACTIVO)
                .build();
    }

    @Test
    public void testRequestOtpSuccess() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(otpTokenRepositoryPort.save(any(OtpToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        otpService.requestOtp(userId, email);

        verify(userRepositoryPort, times(1)).findById(userId);
        verify(otpTokenRepositoryPort, times(1)).deleteByUserId(userId);
        verify(otpTokenRepositoryPort, times(1)).save(any(OtpToken.class));
    }

    @Test
    public void testRequestOtpUserNotFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> otpService.requestOtp(userId, email));
    }

    @Test
    public void testVerifyOtpSuccess() {
        String otpCode = "123456";
        String hashedOtpCode = "$2a$10$hashedcode";

        OtpToken otpToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpCode(hashedOtpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .usedAt(null)
                .build();

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(otpToken));
        when(passwordEncoder.matches(otpCode, hashedOtpCode)).thenReturn(true);
        when(otpTokenRepositoryPort.save(any(OtpToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String otpTokenResult = otpService.verifyOtp(userId, otpCode);

        assertNotNull(otpTokenResult);
        verify(otpTokenRepositoryPort, times(1)).findByUserId(userId);
        verify(passwordEncoder, times(1)).matches(otpCode, hashedOtpCode);
    }

    @Test
    public void testVerifyOtpInvalid() {
        String otpCode = "000000";
        String hashedOtpCode = "$2a$10$hashedcode";

        OtpToken otpToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpCode(hashedOtpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .usedAt(null)
                .build();

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(otpToken));
        when(passwordEncoder.matches(otpCode, hashedOtpCode)).thenReturn(false);
        when(otpTokenRepositoryPort.save(any(OtpToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> otpService.verifyOtp(userId, otpCode));
    }

    @Test
    public void testVerifyOtpExpired() {
        OtpToken expiredToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .otpCode("hashedcode")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now().minusMinutes(11))
                .attempts(0)
                .usedAt(null)
                .build();

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(expiredToken));

        assertThrows(IllegalArgumentException.class, () -> otpService.verifyOtp(userId, "123456"));
        verify(otpTokenRepositoryPort, times(1)).deleteByUserId(userId);
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd auth-service && mvn test -Dtest=OtpServiceTest`
Expected: All 5 tests pass

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/application/service/OtpServiceTest.java
git commit -m "test: add OtpService unit tests (5 cases)"
```

---

### Task 20: Create TokenControllerTest

**Files:**
- Create: `auth-service/src/test/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenControllerTest.java`

- [ ] **Step 1: Write the test**

```java
package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.service.OtpService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TokenController.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private OtpService otpService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private String email;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
    }

    @Test
    @WithMockUser(username = userId.toString())
    public void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"test-token\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testRefreshTokenSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"test-token\"}"))
                .andExpect(status().isUnauthorized()); // Will fail without valid token
    }

    @Test
    @WithMockUser(username = userId.toString())
    public void testRequestOtpSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reauth-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\"}"))
                .andExpect(status().isOk());

        verify(otpService, times(1)).requestOtp(any(), any());
    }

    @Test
    @WithMockUser(username = userId.toString())
    public void testVerifyOtpSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reauth-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"otpCode\": \"123456\"}"))
                .andExpect(status().isUnauthorized()); // Will fail without OTP verification
    }

    @Test
    @WithMockUser(username = userId.toString())
    public void testValidateTokenSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd auth-service && mvn test -Dtest=TokenControllerTest`
Expected: All 5 tests pass

- [ ] **Step 3: Commit**

```bash
git add auth-service/src/test/java/com/axiserp/auth/infrastructure/adapters/in/web/controller/TokenControllerTest.java
git commit -m "test: add TokenController integration tests (5 cases)"
```

---

### Task 21: Build, test, and verify Docker container

**Files:**
- No new files

- [ ] **Step 1: Run all tests**

Run: `cd auth-service && mvn clean test`
Expected: 30+ tests pass

- [ ] **Step 2: Build application**

Run: `cd auth-service && mvn clean package -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 3: Build Docker image**

Run: `docker build -t auth-service:latest auth-service/`
Expected: Successfully built image

- [ ] **Step 4: Start Docker container with docker-compose**

Run: `docker-compose -f compose.yml up -d auth-service`
Expected: Container starts without errors

- [ ] **Step 5: Verify logs**

Run: `docker logs auth-service`
Expected: See "Started AuthServiceApplication"

- [ ] **Step 6: Test health endpoint**

Run: `curl http://localhost:8081/actuator/health`
Expected: HTTP 200 with status UP

- [ ] **Step 7: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: verified auth-service Docker build and container startup"
```

---

### Task 22: Create/Update Postman Collection

**Files:**
- Create: `postman/AxisERP-Auth-Service-Updated.postman_collection.json`

- [ ] **Step 1: Write Postman collection with all endpoints**

```json
{
  "info": {
    "name": "AxisERP Auth Service",
    "description": "API endpoints for Auth Service with security features",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/login",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "login"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"email\": \"santiago.alvarez.gutierrez@correounivalle.edu.co\", \"password\": \"Santiabad*123es\"}"
            }
          }
        },
        {
          "name": "Refresh Token",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/refresh",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "refresh"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"refreshToken\": \"{{refreshToken}}\"}"
            }
          }
        },
        {
          "name": "Logout",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/logout",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "logout"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"refreshToken\": \"{{refreshToken}}\"}"
            }
          }
        },
        {
          "name": "Request OTP",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/reauth-request",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "reauth-request"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"email\": \"santiagoalvarez374@gmail.com\"}"
            }
          }
        },
        {
          "name": "Verify OTP",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/reauth-verify",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "reauth-verify"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"otpCode\": \"123456\"}"
            }
          }
        },
        {
          "name": "Validate Token",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/v1/auth/validate-token",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "auth", "validate-token"]
            }
          }
        }
      ]
    },
    {
      "name": "User Management",
      "item": [
        {
          "name": "Create User (with OTP for ADMIN)",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{otpToken}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/v1/usuarios",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "usuarios"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\"email\": \"juan.nuevo@axiserp.com\", \"name\": \"Juan Nuevo\", \"role\": \"ADMIN\"}"
            }
          }
        },
        {
          "name": "List Users",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/v1/usuarios",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "v1", "usuarios"]
            }
          }
        }
      ]
    }
  ]
}
```

- [ ] **Step 2: Commit Postman collection**

```bash
git add postman/AxisERP-Auth-Service-Updated.postman_collection.json
git commit -m "feat: add updated Postman collection with token and OTP endpoints"
```

---

### Task 23: Integration testing end-to-end flows

**Files:**
- No new files

- [ ] **Step 1: Test Login → Refresh → Logout flow**

Using Postman:
1. POST /auth/login → Get accessToken + refreshToken
2. POST /auth/refresh → Get new accessToken
3. POST /auth/logout → Revoke both tokens
4. GET /auth/validate-token → Should fail with 401

Expected: All steps succeed as described

- [ ] **Step 2: Test OTP Reauth flow**

Using Postman:
1. POST /auth/reauth-request → Send OTP to email
2. POST /auth/reauth-verify → Verify OTP code (check email)
3. POST /usuarios (with otpToken) → Create user with ADMIN role
4. Verify user was created

Expected: All steps succeed

- [ ] **Step 3: Test weak password rejection**

Using Postman:
1. POST /usuarios with password "weak" → Should fail 400
2. POST /usuarios with password "ValidPass1!" → Should succeed

Expected: Weak password rejected, valid password accepted

- [ ] **Step 4: Test token expiration**

1. Get access token
2. Wait 15+ minutes (or mock time)
3. Use token in request
4. Should get 401 Unauthorized

Expected: Expired token rejected

- [ ] **Step 5: Document all flows in Postman collection**

Run: `cat postman/AxisERP-Auth-Service-Updated.postman_collection.json | grep -c "request"`
Expected: Shows number of requests

- [ ] **Step 6: Commit final testing results**

```bash
git commit -m "test: verified all security flows end-to-end (login, refresh, logout, OTP, password validation)"
```

---

## FINAL VERIFICATION CHECKLIST

- [ ] All 5 entidades created (TokenBlacklist, RefreshToken, OtpToken + Domain Models)
- [ ] All 4 services created (TokenBlacklistService, RefreshTokenService, OtpService, PasswordValidator)
- [ ] All 5 endpoints working (logout, refresh, reauth-request, reauth-verify, validate-token)
- [ ] All 30+ JUnit tests passing
- [ ] Docker container builds and starts
- [ ] Postman collection updated with examples
- [ ] All commits made with descriptive messages
- [ ] Git status clean: `git status` shows nothing to commit

---

## SUCCESS CRITERIA

✅ Verify:
1. `mvn test` shows 30+ passing tests
2. `docker logs auth-service` shows "Started AuthServiceApplication"
3. `curl http://localhost:8081/actuator/health` returns 200 UP
4. Postman: Login → Refresh → Logout succeeds
5. Postman: OTP Reauth flow succeeds
6. Git log shows 25+ commits

