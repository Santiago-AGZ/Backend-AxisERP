package com.axiserp.inventory.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

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

import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaMovementRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaMovementRepositoryTest {

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
    private JpaMovementRepository jpaMovementRepository;

    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        jpaMovementRepository.deleteAll();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find movements by product id")
    void findByProductIdOrderByCreatedAtDesc() {
        jpaMovementRepository.save(InventoryMovementEntity.builder()
                .productId(productId)
                .movementType(MovementType.ENTRADA)
                .quantity(10)
                .previousStock(0)
                .newStock(10)
                .userId(userId)
                .build());
        jpaMovementRepository.save(InventoryMovementEntity.builder()
                .productId(productId)
                .movementType(MovementType.SALIDA)
                .quantity(3)
                .previousStock(10)
                .newStock(7)
                .userId(userId)
                .build());

        var movements = jpaMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
        assertEquals(2, movements.size());
        assertTrue(movements.get(0).getCreatedAt().isAfter(movements.get(1).getCreatedAt())
                || movements.get(0).getCreatedAt().equals(movements.get(1).getCreatedAt()));
    }

    @Test
    @DisplayName("Should count movements by product id")
    void countByProductId() {
        jpaMovementRepository.save(InventoryMovementEntity.builder()
                .productId(productId)
                .movementType(MovementType.ENTRADA)
                .quantity(10).previousStock(0).newStock(10)
                .userId(userId)
                .build());
        jpaMovementRepository.save(InventoryMovementEntity.builder()
                .productId(productId)
                .movementType(MovementType.SALIDA)
                .quantity(5).previousStock(10).newStock(5)
                .userId(userId)
                .build());

        assertEquals(2, jpaMovementRepository.countByProductId(productId));
    }

    @Test
    @DisplayName("Should save movement with all fields")
    void saveMovement() {
        InventoryMovementEntity saved = jpaMovementRepository.save(InventoryMovementEntity.builder()
                .productId(productId)
                .inventoryId(UUID.randomUUID())
                .movementType(MovementType.AJUSTE_POSITIVO)
                .quantity(5)
                .previousStock(10)
                .newStock(15)
                .referenceType("ADJUSTMENT")
                .referenceId(UUID.randomUUID())
                .justification("Inventory count correction")
                .notes("Found extra units")
                .userId(userId)
                .build());

        assertNotNull(saved.getId());
        assertEquals(MovementType.AJUSTE_POSITIVO, saved.getMovementType());
        assertEquals("Inventory count correction", saved.getJustification());
    }
}
