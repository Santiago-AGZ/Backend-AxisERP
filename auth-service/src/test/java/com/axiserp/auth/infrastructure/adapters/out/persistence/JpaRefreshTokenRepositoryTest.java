package com.axiserp.auth.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RefreshTokenEntity.TokenStatus;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRefreshTokenRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRefreshTokenRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withInitScript("init-citext.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private JpaRefreshTokenRepository jpaRefreshTokenRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        jpaRefreshTokenRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find refresh token by token string")
    void findByToken() {
        String tokenValue = UUID.randomUUID().toString();
        jpaRefreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        var found = jpaRefreshTokenRepository.findByToken(tokenValue);
        assertTrue(found.isPresent());
        assertEquals(tokenValue, found.get().getToken());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    @DisplayName("Should find refresh token by userId and token")
    void findByUserIdAndToken() {
        String tokenValue = UUID.randomUUID().toString();
        jpaRefreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        var found = jpaRefreshTokenRepository.findByUserIdAndToken(userId, tokenValue);
        assertTrue(found.isPresent());
        assertEquals(tokenValue, found.get().getToken());
    }

    @Test
    @DisplayName("Should update token status to revoked")
    void revokeToken() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshTokenEntity saved = jpaRefreshTokenRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        saved.setStatus(TokenStatus.REVOKED);
        saved.setRevokedAt(LocalDateTime.now());
        jpaRefreshTokenRepository.save(saved);

        var found = jpaRefreshTokenRepository.findByToken(tokenValue);
        assertTrue(found.isPresent());
        assertEquals(TokenStatus.REVOKED, found.get().getStatus());
        assertNotNull(found.get().getRevokedAt());
    }
}
