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
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseItemEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaPurchaseItemRepository;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaPurchaseRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaPurchaseItemRepositoryTest {

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
    private JpaPurchaseItemRepository jpaPurchaseItemRepository;

    @Autowired
    private JpaPurchaseRepository jpaPurchaseRepository;

    private UUID purchaseId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        jpaPurchaseItemRepository.deleteAll();
        jpaPurchaseRepository.deleteAll();
        purchaseId = jpaPurchaseRepository.save(PurchaseEntity.builder()
                .id(UUID.randomUUID())
                .supplierId(UUID.randomUUID())
                .purchaseNumber("PARENT-PO-" + UUID.randomUUID().toString().substring(0, 6))
                .status(PurchaseEntity.PurchaseStatus.BORRADOR)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build()).getId();
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find item by purchase id")
    void findByPurchaseId() {
        jpaPurchaseItemRepository.save(PurchaseItemEntity.builder()
                .id(UUID.randomUUID())
                .purchaseId(purchaseId)
                .productId(productId)
                .productName("Item A E1")
                .quantity(5)
                .receivedQuantity(3)
                .unitPrice(BigDecimal.valueOf(20))
                .subtotal(BigDecimal.valueOf(100))
                .build());

        var items = jpaPurchaseItemRepository.findByPurchaseId(purchaseId);
        assertEquals(1, items.size());
        assertEquals("Item A E1", items.get(0).getProductName());
    }

    @Test
    @DisplayName("Should save multiple items for same purchase")
    void multipleItemsForPurchase() {
        jpaPurchaseItemRepository.save(PurchaseItemEntity.builder()
                .id(UUID.randomUUID()).purchaseId(purchaseId)
                .productId(productId)
                .productName("Item 1 E2").quantity(2).receivedQuantity(2)
                .unitPrice(BigDecimal.valueOf(50)).subtotal(BigDecimal.valueOf(100))
                .build());
        jpaPurchaseItemRepository.save(PurchaseItemEntity.builder()
                .id(UUID.randomUUID()).purchaseId(purchaseId)
                .productId(UUID.randomUUID())
                .productName("Item 2 E3").quantity(1).receivedQuantity(1)
                .unitPrice(BigDecimal.valueOf(30)).subtotal(BigDecimal.valueOf(30))
                .build());

        var items = jpaPurchaseItemRepository.findByPurchaseId(purchaseId);
        assertEquals(2, items.size());
    }

    @Test
    @DisplayName("Should return empty list for nonexistent purchase")
    void findByPurchaseId_notFound() {
        var items = jpaPurchaseItemRepository.findByPurchaseId(UUID.randomUUID());
        assertTrue(items.isEmpty());
    }
}
