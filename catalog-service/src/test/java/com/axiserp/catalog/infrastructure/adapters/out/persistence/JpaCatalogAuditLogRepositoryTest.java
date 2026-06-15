package com.axiserp.catalog.infrastructure.adapters.out.persistence;

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

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CatalogAuditLogEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaCatalogAuditLogRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCatalogAuditLogRepositoryTest {

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
    private JpaCatalogAuditLogRepository jpaCatalogAuditLogRepository;

    @BeforeEach
    void setUp() {
        jpaCatalogAuditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find audit log by entity type")
    void findByEntityType() {
        UUID entityId = UUID.randomUUID();
        jpaCatalogAuditLogRepository.save(CatalogAuditLogEntity.builder()
                .userId(UUID.randomUUID())
                .action("CREATE")
                .entityType("Product")
                .entityId(entityId)
                .timestamp(LocalDateTime.now())
                .build());

        var all = jpaCatalogAuditLogRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("Product", all.get(0).getEntityType());
    }

    @Test
    @DisplayName("Should save audit log with entity id")
    void findByEntityId() {
        UUID productId = UUID.randomUUID();
        jpaCatalogAuditLogRepository.save(CatalogAuditLogEntity.builder()
                .userId(UUID.randomUUID())
                .action("UPDATE")
                .entityType("Product")
                .entityId(productId)
                .timestamp(LocalDateTime.now())
                .build());

        var found = jpaCatalogAuditLogRepository.findAll().stream()
                .filter(log -> productId.equals(log.getEntityId()))
                .findFirst();
        assertTrue(found.isPresent());
        assertEquals("UPDATE", found.get().getAction());
    }

    @Test
    @DisplayName("Should persist all audit log fields")
    void saveAuditLog() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        CatalogAuditLogEntity saved = jpaCatalogAuditLogRepository.save(CatalogAuditLogEntity.builder()
                .userId(userId)
                .action("DELETE")
                .entityType("Category")
                .entityId(entityId)
                .detail("{\"reason\":\"cleanup\"}")
                .ipAddress("192.168.1.1")
                .userAgent("Chrome")
                .build());

        assertNotNull(saved.getId());
        assertEquals("DELETE", saved.getAction());
        assertNotNull(saved.getTimestamp());
    }
}
