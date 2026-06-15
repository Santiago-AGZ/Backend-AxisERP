package com.axiserp.inventory.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaInventoryRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaInventoryRepositoryTest {

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
    private JpaInventoryRepository jpaInventoryRepository;

    private UUID productId;
    private UUID productIdB;

    @BeforeEach
    void setUp() {
        jpaInventoryRepository.deleteAll();
        productId = UUID.randomUUID();
        productIdB = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find inventory by product id")
    void findByProductId() {
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(50)
                .minStock(10)
                .maxStock(100)
                .reservedStock(5)
                .build());

        var found = jpaInventoryRepository.findByProductId(productId);
        assertTrue(found.isPresent());
        assertEquals(50, found.get().getCurrentStock());
    }

    @Test
    @DisplayName("Should find low stock items")
    void findLowStock() {
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(3).minStock(10).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdB)
                .currentStock(50).minStock(10).reservedStock(0)
                .build());

        var lowStock = jpaInventoryRepository.findLowStock(10, 0);
        assertEquals(1, lowStock.size());
        assertEquals(productId, lowStock.get(0).getProductId());
    }

    @Test
    @DisplayName("Should count low stock items")
    void countLowStock() {
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(3).minStock(10).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdB)
                .currentStock(7).minStock(10).reservedStock(0)
                .build());

        assertEquals(2, jpaInventoryRepository.countLowStock());
    }

    @Test
    @DisplayName("Should find depleted items")
    void findDepleted() {
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(0).minStock(10).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdB)
                .currentStock(5).minStock(10).reservedStock(0)
                .build());

        var depleted = jpaInventoryRepository.findDepleted(10, 0);
        assertEquals(1, depleted.size());
        assertEquals(productId, depleted.get(0).getProductId());
    }

    @Test
    @DisplayName("Should count depleted items")
    void countDepleted() {
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(0).minStock(10).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdB)
                .currentStock(0).minStock(10).reservedStock(0)
                .build());

        assertEquals(2, jpaInventoryRepository.countDepleted());
    }

    @Test
    @DisplayName("Should find inventory by product ids")
    void findByProductIds() {
        UUID productIdC = UUID.randomUUID();
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(10).minStock(5).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdB)
                .currentStock(20).minStock(5).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdC)
                .currentStock(30).minStock(5).reservedStock(0)
                .build());

        var found = jpaInventoryRepository.findByProductIds(
                List.of(productId, productIdB), PageRequest.of(0, 10));
        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("Should count by product ids")
    void countByProductIds() {
        UUID productIdC = UUID.randomUUID();
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(10).minStock(5).reservedStock(0)
                .build());
        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productIdC)
                .currentStock(30).minStock(5).reservedStock(0)
                .build());

        assertEquals(2, jpaInventoryRepository.countByProductIds(List.of(productId, productIdC)));
    }

    @Test
    @DisplayName("Should increment version on update")
    void versionIncrement() {
        InventoryEntity saved = jpaInventoryRepository.saveAndFlush(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(10).minStock(5).reservedStock(0)
                .build());

        Long versionBefore = saved.getVersion();

        saved.setCurrentStock(20);
        jpaInventoryRepository.saveAndFlush(saved);

        InventoryEntity updated = jpaInventoryRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(versionBefore);
        assertNotNull(updated.getVersion());
        assertTrue(updated.getVersion() > versionBefore);
    }

    @Test
    @DisplayName("Should enforce unique productId constraint")
    void uniqueProductId() {
        jpaInventoryRepository.saveAndFlush(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(10).minStock(5).reservedStock(0)
                .build());

        assertThrows(Exception.class, () -> jpaInventoryRepository.saveAndFlush(InventoryEntity.builder()
                .id(UUID.randomUUID()).productId(productId)
                .currentStock(20).minStock(5).reservedStock(0)
                .build()));
    }
}
