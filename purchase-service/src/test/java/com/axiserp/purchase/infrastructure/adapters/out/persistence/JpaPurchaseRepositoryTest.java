package com.axiserp.purchase.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
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

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity.PurchaseStatus;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaPurchaseRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaPurchaseRepositoryTest {

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
    private JpaPurchaseRepository jpaPurchaseRepository;

    private UUID supplierId;
    private UUID supplierIdB;

    @BeforeEach
    void setUp() {
        jpaPurchaseRepository.deleteAll();
        supplierId = UUID.randomUUID();
        supplierIdB = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find purchase by supplier id")
    void findBySupplierId() {
        jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID())
                .supplierId(supplierId)
                .purchaseNumber("PO-001-D1")
                .status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100))
                .tax(BigDecimal.valueOf(19))
                .total(BigDecimal.valueOf(119))
                .build());

        var results = jpaPurchaseRepository.findBySupplierId(supplierId);
        assertEquals(1, results.size());
        assertEquals("PO-001-D1", results.get(0).getPurchaseNumber());
    }

    @Test
    @DisplayName("Should find purchases by status ordered by creation date")
    void findByStatusOrderByCreatedAtDesc() {
        jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-002-D2").status(PurchaseStatus.PENDIENTE)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        var results = jpaPurchaseRepository.findByStatusOrderByCreatedAtDesc(PurchaseStatus.PENDIENTE);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should check existence by purchase number")
    void existsByPurchaseNumber() {
        jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-UNIQUE-D3").status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        assertTrue(jpaPurchaseRepository.existsByPurchaseNumber("PO-UNIQUE-D3"));
        assertFalse(jpaPurchaseRepository.existsByPurchaseNumber("NONEXISTENT-PO"));
    }

    @Test
    @DisplayName("Should find purchases by filters")
    void findByFilters() {
        jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-SEARCH-D4").status(PurchaseStatus.PENDIENTE)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        var byStatus = jpaPurchaseRepository.findByFilters(null, "PENDIENTE", 10, 0);
        assertEquals(1, byStatus.size());

        var bySearch = jpaPurchaseRepository.findByFilters("SEARCH", null, 10, 0);
        assertEquals(1, bySearch.size());
    }

    @Test
    @DisplayName("Should count purchases by filters")
    void countByFilters() {
        jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-COUNT-D5").status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        assertEquals(1, jpaPurchaseRepository.countByFilters(null, null));
        assertEquals(1, jpaPurchaseRepository.countByFilters(null, "BORRADOR"));
        assertEquals(0, jpaPurchaseRepository.countByFilters(null, "RECIBIDA"));
    }

    @Test
    @DisplayName("Should increment version on update")
    void versionIncrement() {
        PurchaseEntity saved = jpaPurchaseRepository.saveAndFlush(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-VER-D6").status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        Long versionBefore = saved.getVersion();
        saved.setStatus(PurchaseStatus.ENVIADA);
        jpaPurchaseRepository.saveAndFlush(saved);

        PurchaseEntity updated = jpaPurchaseRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(versionBefore);
        assertNotNull(updated.getVersion());
        assertTrue(updated.getVersion() > versionBefore);
    }

    @Test
    @DisplayName("Should enforce unique purchaseNumber constraint")
    void uniquePurchaseNumber() {
        jpaPurchaseRepository.saveAndFlush(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-UNIQUE-CONSTRAINT-D7").status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build());

        assertThrows(Exception.class, () -> jpaPurchaseRepository.saveAndFlush(PurchaseEntity.builder()
                .id(UUID.randomUUID()).supplierId(supplierId)
                .purchaseNumber("PO-UNIQUE-CONSTRAINT-D7").status(PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .build()));
    }
}
