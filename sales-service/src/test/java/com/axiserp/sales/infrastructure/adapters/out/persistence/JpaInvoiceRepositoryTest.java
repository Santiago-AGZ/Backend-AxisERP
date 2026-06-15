package com.axiserp.sales.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
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

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.InvoiceEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaInvoiceRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaInvoiceRepositoryTest {

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
    private JpaInvoiceRepository jpaInvoiceRepository;

    private UUID saleId;

    @BeforeEach
    void setUp() {
        jpaInvoiceRepository.deleteAll();
        saleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find invoice by sale id")
    void findBySaleId() {
        jpaInvoiceRepository.save(InvoiceEntity.builder()
                .saleId(saleId)
                .invoiceNumber(1001L)
                .customerSnapshot("{\"name\":\"Cliente I1\"}")
                .itemsSnapshot("[]")
                .subtotal(BigDecimal.valueOf(100))
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19))
                .total(BigDecimal.valueOf(119))
                .issuedAt(LocalDateTime.now())
                .build());

        var found = jpaInvoiceRepository.findBySaleId(saleId);
        assertTrue(found.isPresent());
        assertEquals(1001L, found.get().getInvoiceNumber());
    }

    @Test
    @DisplayName("Should return empty for nonexistent sale id")
    void findBySaleId_notFound() {
        var found = jpaInvoiceRepository.findBySaleId(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should enforce unique saleId constraint")
    void uniqueSaleId() {
        jpaInvoiceRepository.saveAndFlush(InvoiceEntity.builder()
                .saleId(saleId).invoiceNumber(2001L)
                .customerSnapshot("{}").itemsSnapshot("[]")
                .subtotal(BigDecimal.valueOf(100)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(19)).total(BigDecimal.valueOf(119))
                .issuedAt(LocalDateTime.now())
                .build());

        assertThrows(Exception.class, () -> jpaInvoiceRepository.saveAndFlush(InvoiceEntity.builder()
                .saleId(saleId).invoiceNumber(2002L)
                .customerSnapshot("{}").itemsSnapshot("[]")
                .subtotal(BigDecimal.valueOf(200)).discount(BigDecimal.ZERO)
                .tax(BigDecimal.valueOf(38)).total(BigDecimal.valueOf(238))
                .issuedAt(LocalDateTime.now())
                .build()));
    }
}
