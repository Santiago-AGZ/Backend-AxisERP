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

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.PasswordResetTokenEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaPasswordResetTokenRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaPasswordResetTokenRepositoryTest {

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
    private JpaPasswordResetTokenRepository jpaPasswordResetTokenRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        jpaPasswordResetTokenRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find password reset token")
    void findByToken() {
        String tokenValue = UUID.randomUUID().toString();
        jpaPasswordResetTokenRepository.save(PasswordResetTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        var found = jpaPasswordResetTokenRepository.findByToken(tokenValue);
        assertTrue(found.isPresent());
        assertEquals(tokenValue, found.get().getToken());
        assertEquals(userId, found.get().getUserId());
        assertFalse(found.get().isUsed());
    }

    @Test
    @DisplayName("Should mark token as used")
    void usedToken() {
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetTokenEntity saved = jpaPasswordResetTokenRepository.save(PasswordResetTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build());

        saved.setUsed(true);
        saved.setUsedAt(LocalDateTime.now());
        jpaPasswordResetTokenRepository.save(saved);

        var found = jpaPasswordResetTokenRepository.findByToken(tokenValue);
        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
        assertNotNull(found.get().getUsedAt());
    }

    @Test
    @DisplayName("Should return empty for expired token")
    void expiredToken() {
        String tokenValue = UUID.randomUUID().toString();
        jpaPasswordResetTokenRepository.save(PasswordResetTokenEntity.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build());

        var found = jpaPasswordResetTokenRepository.findByToken(tokenValue);
        assertTrue(found.isPresent());
        assertTrue(found.get().getExpiresAt().isBefore(LocalDateTime.now()));
    }
}
