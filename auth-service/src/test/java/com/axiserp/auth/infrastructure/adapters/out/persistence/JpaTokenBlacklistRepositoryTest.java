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

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.TokenBlacklistEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaTokenBlacklistRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaTokenBlacklistRepositoryTest {

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
    private JpaTokenBlacklistRepository jpaTokenBlacklistRepository;

    @BeforeEach
    void setUp() {
        jpaTokenBlacklistRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find token by jti")
    void findByTokenJti() {
        String jti = UUID.randomUUID().toString();
        jpaTokenBlacklistRepository.save(TokenBlacklistEntity.builder()
                .tokenJti(jti)
                .userId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());

        var found = jpaTokenBlacklistRepository.findByTokenJti(jti);
        assertTrue(found.isPresent());
        assertEquals(jti, found.get().getTokenJti());
    }

    @Test
    @DisplayName("Should return true when token exists")
    void existsByTokenJti() {
        String jti = UUID.randomUUID().toString();
        jpaTokenBlacklistRepository.save(TokenBlacklistEntity.builder()
                .tokenJti(jti)
                .userId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());

        assertTrue(jpaTokenBlacklistRepository.existsByTokenJti(jti));
        assertFalse(jpaTokenBlacklistRepository.existsByTokenJti("nonexistent-jti"));
    }

    @Test
    @DisplayName("Should return empty when token jti not found")
    void findByTokenJti_notFound() {
        var found = jpaTokenBlacklistRepository.findByTokenJti("unknown-jti");
        assertTrue(found.isEmpty());
    }
}
