package com.axiserp.catalog.infrastructure.adapters.out.persistence;

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

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.ProductEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.ProductEntity.ProductStatus;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaProductRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaProductRepositoryTest {

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
    private JpaProductRepository jpaProductRepository;

    private UUID categoryId;
    private UUID categoryIdB;

    @BeforeEach
    void setUp() {
        jpaProductRepository.deleteAll();
        categoryId = UUID.randomUUID();
        categoryIdB = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find product by codigo")
    void findByCodigo() {
        String codigo = "PROD-TEST-A1";
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Product A1")
                .codigo(codigo)
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.valueOf(15))
                .status(ProductStatus.ACTIVO)
                .build());

        var found = jpaProductRepository.findByCodigo(codigo);
        assertTrue(found.isPresent());
        assertEquals("Test Product A1", found.get().getName());
    }

    @Test
    @DisplayName("Should return true when codigo exists")
    void existsByCodigo() {
        String codigo = "PROD-EXISTS-A2";
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Exists Test A2")
                .codigo(codigo)
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.TEN)
                .salePrice(BigDecimal.valueOf(15))
                .status(ProductStatus.ACTIVO)
                .build());

        assertTrue(jpaProductRepository.existsByCodigo(codigo));
        assertFalse(jpaProductRepository.existsByCodigo("NONEXISTENT-CODIGO"));
    }

    @Test
    @DisplayName("Should count by category id")
    void countByCategoryId() {
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("P1").codigo("P-COUNT-1")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("P2").codigo("P-COUNT-2")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("P3").codigo("P-COUNT-3")
                .categoryId(categoryIdB)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());

        assertEquals(2, jpaProductRepository.countByCategoryId(categoryId));
        assertEquals(1, jpaProductRepository.countByCategoryId(categoryIdB));
    }

    @Test
    @DisplayName("Should count active products by category id")
    void countActiveByCategoryId() {
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Active P").codigo("P-ACT-1")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Inactive P").codigo("P-ACT-2")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.INACTIVO).build());

        assertEquals(1, jpaProductRepository.countActiveByCategoryId(categoryId));
    }

    @Test
    @DisplayName("Should find products by filters with search, codigo, categoryId")
    void findByFilters() {
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Widget Alpha").codigo("W-A3")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.TEN).salePrice(BigDecimal.valueOf(15))
                .status(ProductStatus.ACTIVO).build());
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Widget Beta").codigo("W-B4")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.TEN).salePrice(BigDecimal.valueOf(15))
                .status(ProductStatus.ACTIVO).build());

        var byCategory = jpaProductRepository.findByFilters(null, null, categoryId.toString(), true, 10, 0);
        assertEquals(2, byCategory.size());

        var bySearch = jpaProductRepository.findByFilters("Alpha", null, null, true, 10, 0);
        assertEquals(1, bySearch.size());
    }

    @Test
    @DisplayName("Should count products by filters")
    void countByFilters() {
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Filter Count A5").codigo("FC-A5")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());
        jpaProductRepository.save(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Filter Count B6").codigo("FC-B6")
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());

        long total = jpaProductRepository.countByFilters(null, null, null, true);
        assertEquals(2, total);
    }

    @Test
    @DisplayName("Should enforce unique codigo constraint")
    void uniqueCodigo() {
        String codigo = "UNIQUE-COD-A7";
        jpaProductRepository.saveAndFlush(ProductEntity.builder()
                .id(UUID.randomUUID()).name("First").codigo(codigo)
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build());

        assertThrows(Exception.class, () -> jpaProductRepository.saveAndFlush(ProductEntity.builder()
                .id(UUID.randomUUID()).name("Second").codigo(codigo)
                .categoryId(categoryId)
                .purchasePrice(BigDecimal.ONE).salePrice(BigDecimal.valueOf(2))
                .status(ProductStatus.ACTIVO).build()));
    }
}
