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

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.AuditLogEntity.AuditAction;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaAuditLogRepositoryTest {

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
    private JpaAuditLogRepository jpaAuditLogRepository;

    private UUID userIdA;
    private UUID userIdB;

    @BeforeEach
    void setUp() {
        jpaAuditLogRepository.deleteAll();
        userIdA = UUID.randomUUID();
        userIdB = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find audit log by userId filter")
    void findByUserId() {
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdA)
                .action(AuditAction.LOGIN)
                .entityType("User")
                .entityId(userIdA)
                .build());
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdB)
                .action(AuditAction.LOGIN)
                .entityType("User")
                .entityId(userIdB)
                .build());

        var resultA = jpaAuditLogRepository.findByFilters(userIdA, null, null, 0, 10);
        var resultB = jpaAuditLogRepository.findByFilters(userIdB, null, null, 0, 10);
        var all = jpaAuditLogRepository.findByFilters(null, null, null, 0, 10);

        assertEquals(1, resultA.getTotalElements());
        assertEquals(1, resultB.getTotalElements());
        assertEquals(2, all.getTotalElements());
    }

    @Test
    @DisplayName("Should filter by action")
    void findByAction() {
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdA)
                .action(AuditAction.LOGIN)
                .entityType("User")
                .entityId(userIdA)
                .build());
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdA)
                .action(AuditAction.LOGOUT)
                .entityType("User")
                .entityId(userIdA)
                .build());

        var loginResults = jpaAuditLogRepository.findByFilters(null, AuditAction.LOGIN, null, 0, 10);
        var logoutResults = jpaAuditLogRepository.findByFilters(null, AuditAction.LOGOUT, null, 0, 10);

        assertEquals(1, loginResults.getTotalElements());
        assertEquals(1, logoutResults.getTotalElements());
    }

    @Test
    @DisplayName("Should order by timestamp descending")
    void ordering() {
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdA)
                .action(AuditAction.CREATE)
                .entityType("User")
                .entityId(userIdA)
                .build());
        jpaAuditLogRepository.save(AuditLogEntity.builder()
                .userId(userIdA)
                .action(AuditAction.UPDATE)
                .entityType("User")
                .entityId(userIdA)
                .build());

        var results = jpaAuditLogRepository.findByFilters(null, null, null, 0, 10);
        assertTrue(results.getContent().get(0).getTimestamp()
                .isAfter(results.getContent().get(1).getTimestamp())
                || results.getContent().get(0).getTimestamp()
                        .equals(results.getContent().get(1).getTimestamp()));
    }
}
