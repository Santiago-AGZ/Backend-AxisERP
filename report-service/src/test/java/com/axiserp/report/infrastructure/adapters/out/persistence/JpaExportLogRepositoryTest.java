package com.axiserp.report.infrastructure.adapters.out.persistence;

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

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import com.axiserp.report.infrastructure.adapters.out.persistence.repository.JpaExportLogRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaExportLogRepositoryTest {

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
    private JpaExportLogRepository jpaExportLogRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        jpaExportLogRepository.deleteAll();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find all export logs ordered by generatedAt desc")
    void findAllByOrderByGeneratedAtDesc() {
        jpaExportLogRepository.save(ExportLogEntity.builder()
                .userId(userId)
                .reportType("INVENTORY_REPORT")
                .format("PDF")
                .recordCount(100)
                .fileSizeBytes(2048L)
                .build());

        var logs = jpaExportLogRepository.findAllByOrderByGeneratedAtDesc();
        assertEquals(1, logs.size());
        assertEquals("INVENTORY_REPORT", logs.get(0).getReportType());
    }

    @Test
    @DisplayName("Should find export logs by generatedAt between")
    void findByGeneratedAtBetween() {
        jpaExportLogRepository.save(ExportLogEntity.builder()
                .userId(userId)
                .reportType("SALES_REPORT")
                .format("XLSX")
                .recordCount(50)
                .build());

        LocalDateTime from = LocalDateTime.now().minusMinutes(5);
        LocalDateTime to = LocalDateTime.now().plusMinutes(5);

        var logs = jpaExportLogRepository.findByGeneratedAtBetweenOrderByGeneratedAtDesc(from, to);
        assertEquals(1, logs.size());
        assertEquals("SALES_REPORT", logs.get(0).getReportType());
    }

    @Test
    @DisplayName("Should return empty list when no logs in date range")
    void findByGeneratedAtBetween_noResults() {
        var logs = jpaExportLogRepository.findByGeneratedAtBetweenOrderByGeneratedAtDesc(
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        assertTrue(logs.isEmpty());
    }

    @Test
    @DisplayName("Should find export logs by report type")
    void findByReportType() {
        jpaExportLogRepository.save(ExportLogEntity.builder()
                .userId(userId)
                .reportType("PURCHASE_REPORT")
                .format("CSV")
                .recordCount(75)
                .build());

        var logs = jpaExportLogRepository.findByReportTypeOrderByGeneratedAtDesc("PURCHASE_REPORT");
        assertEquals(1, logs.size());
        assertEquals("CSV", logs.get(0).getFormat());
    }
}
