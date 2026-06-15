package com.axiserp.sales.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
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

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity.SaleStatus;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaSaleRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaSaleRepositoryTest {

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
    private JpaSaleRepository jpaSaleRepository;

    private UUID customerId;
    private UUID customerIdB;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        jpaSaleRepository.deleteAll();
        customerId = UUID.randomUUID();
        customerIdB = UUID.randomUUID();
        createdBy = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find sale by sale number")
    void findBySaleNumber() {
        jpaSaleRepository.save(SaleEntity.builder()
                .customerId(customerId)
                .saleNumber("SALE-G1")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(200))
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(38))
                .total(BigDecimal.valueOf(238))
                .createdBy(createdBy)
                .build());

        var found = jpaSaleRepository.findBySaleNumber("SALE-G1");
        assertTrue(found.isPresent());
        assertEquals(customerId, found.get().getCustomerId());
    }

    @Test
    @DisplayName("Should find sales by customer id ordered by creation date")
    void findByCustomerIdOrderByCreatedAtDesc() {
        jpaSaleRepository.save(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-CUST-G2")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        var results = jpaSaleRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, createdBy);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should check existence by customer id and status in")
    void existsByCustomerIdAndStatusIn() {
        jpaSaleRepository.save(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-EXISTS-G3")
                .status(SaleStatus.PENDIENTE)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        boolean exists = jpaSaleRepository.existsByCustomerIdAndStatusIn(
                customerId, List.of(SaleStatus.PENDIENTE, SaleStatus.CONFIRMADA));
        assertTrue(exists);

        boolean notExists = jpaSaleRepository.existsByCustomerIdAndStatusIn(
                customerIdB, List.of(SaleStatus.PENDIENTE));
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should find sales by filters with customerId and status")
    void findByFilters() {
        jpaSaleRepository.save(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-FILTER-G4")
                .status(SaleStatus.CONFIRMADA)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        var byCustomer = jpaSaleRepository.findByFilters(customerId, null, null, null, null, PageRequest.of(0, 10));
        assertTrue(byCustomer.size() >= 1);

        var byStatus = jpaSaleRepository.findByFilters(null, "CONFIRMADA", null, null, null, PageRequest.of(0, 10));
        assertEquals(1, byStatus.size());
    }

    @Test
    @DisplayName("Should count sales by filters")
    void countByFilters() {
        jpaSaleRepository.save(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-COUNT-G5")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        long count = jpaSaleRepository.countByFilters(customerId, null, null, null, null);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should enforce unique saleNumber constraint")
    void uniqueSaleNumber() {
        jpaSaleRepository.saveAndFlush(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-UNIQUE-G6")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        assertThrows(Exception.class, () -> jpaSaleRepository.saveAndFlush(SaleEntity.builder()
                .customerId(customerIdB).saleNumber("SALE-UNIQUE-G6")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(200)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(38)).total(BigDecimal.valueOf(238))
                .createdBy(createdBy)
                .build()));
    }

    @Test
    @DisplayName("Should increment version on update")
    void versionIncrement() {
        SaleEntity saved = jpaSaleRepository.saveAndFlush(SaleEntity.builder()
                .customerId(customerId).saleNumber("SALE-VER-G7")
                .status(SaleStatus.BORRADOR)
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .createdBy(createdBy)
                .build());

        Long versionBefore = saved.getVersion();
        saved.setStatus(SaleStatus.CONFIRMADA);
        jpaSaleRepository.saveAndFlush(saved);

        SaleEntity updated = jpaSaleRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(versionBefore);
        assertNotNull(updated.getVersion());
        assertTrue(updated.getVersion() > versionBefore);
    }
}
