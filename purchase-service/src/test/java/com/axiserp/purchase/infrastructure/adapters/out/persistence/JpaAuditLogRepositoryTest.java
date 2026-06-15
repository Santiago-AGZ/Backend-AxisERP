package com.axiserp.purchase.infrastructure.adapters.out.persistence;

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

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.AuditLogEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaAuditLogRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaAuditLogRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

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

    @BeforeEach
    void setUp() {
        jpaAuditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find audit log")
    void saveAndFind() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuditLogEntity saved = jpaAuditLogRepository.save(AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("PURCHASE_CREATED")
                .entityType("Purchase")
                .entityId(entityId)
                .detail("Purchase order created")
                .userId(userId)
                .ipAddress("10.0.0.2")
                .userAgent("Internal Service")
                .build());

        assertNotNull(saved.getId());

        var found = jpaAuditLogRepository.findAll();
        assertEquals(1, found.size());
        assertEquals("PURCHASE_CREATED", found.get(0).getAction());
        assertEquals(entityId, found.get(0).getEntityId());
    }

    @Test
    @DisplayName("Should set timestamp on persist")
    void timestampOnPersist() {
        AuditLogEntity saved = jpaAuditLogRepository.save(AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .action("PURCHASE_UPDATED")
                .entityType("Purchase")
                .build());

        assertNotNull(saved.getTimestamp());
    }
}
