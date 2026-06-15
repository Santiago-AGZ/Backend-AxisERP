package com.axiserp.sales.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

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

import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity.CustomerStatus;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity.DocumentType;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaCustomerRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCustomerRepositoryTest {

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
    private JpaCustomerRepository jpaCustomerRepository;

    @BeforeEach
    void setUp() {
        jpaCustomerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find customer by codigo")
    void findByCodigo() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-H1")
                .name("Cliente Uno H1")
                .documentType(DocumentType.CC)
                .documentNumber("100000001")
                .email("cliente.h1@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        var found = jpaCustomerRepository.findByCodigo("CLI-H1");
        assertTrue(found.isPresent());
        assertEquals("Cliente Uno H1", found.get().getName());
    }

    @Test
    @DisplayName("Should check existence by codigo")
    void existsByCodigo() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-EXISTS-H2").name("Exists Test")
                .documentType(DocumentType.CC).documentNumber("100000002")
                .email("exists.h2@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertTrue(jpaCustomerRepository.existsByCodigo("CLI-EXISTS-H2"));
        assertFalse(jpaCustomerRepository.existsByCodigo("NONEXISTENT"));
    }

    @Test
    @DisplayName("Should check existence by document number")
    void existsByDocumentNumber() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-DOC-H3").name("Doc Test")
                .documentType(DocumentType.NIT).documentNumber("900200003")
                .email("doc.h3@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertTrue(jpaCustomerRepository.existsByDocumentNumber("900200003"));
        assertFalse(jpaCustomerRepository.existsByDocumentNumber("000000000"));
    }

    @Test
    @DisplayName("Should check existence by email")
    void existsByEmail() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-EMAIL-H4").name("Email Test")
                .documentType(DocumentType.CC).documentNumber("100000004")
                .email("unique.h4@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertTrue(jpaCustomerRepository.existsByEmail("unique.h4@test.com"));
        assertFalse(jpaCustomerRepository.existsByEmail("nobody@test.com"));
    }

    @Test
    @DisplayName("Should check existence by email excluding id")
    void existsByEmailAndIdNot() {
        CustomerEntity saved = jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-EMAIL-EXCL-H5").name("Email Exclude")
                .documentType(DocumentType.CC).documentNumber("100000005")
                .email("excl.h5@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertFalse(jpaCustomerRepository.existsByEmailAndIdNot("excl.h5@test.com", saved.getId()));
        assertTrue(jpaCustomerRepository.existsByEmailAndIdNot("excl.h5@test.com", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should find customers by filters with search and includeInactive")
    void findByFilters() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-SEARCH-H6").name("Busqueda Cliente")
                .documentType(DocumentType.CC).documentNumber("100000006")
                .email("search.h6@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        var results = jpaCustomerRepository.findByFilters(
                true, "%busqueda%", true, CustomerStatus.ACTIVO, PageRequest.of(0, 10));
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should count customers by filters")
    void countByFilters() {
        jpaCustomerRepository.save(CustomerEntity.builder()
                .codigo("CLI-COUNT-H7").name("Count Customer")
                .documentType(DocumentType.CC).documentNumber("100000007")
                .email("count.h7@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        long count = jpaCustomerRepository.countByFilters(
                false, "%", true, CustomerStatus.ACTIVO);
        assertEquals(1, count);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should enforce unique codigo constraint")
    void uniqueCodigo() {
        jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-UNIQ-H8").name("First")
                .documentType(DocumentType.CC).documentNumber("100000008")
                .email("uniq.h8@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertThrows(Exception.class, () -> jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-UNIQ-H8").name("Second")
                .documentType(DocumentType.NIT).documentNumber("900200009")
                .email("uniq2.h8@test.com")
                .status(CustomerStatus.ACTIVO)
                .build()));
    }

    @Test
    @DisplayName("Should enforce unique documentNumber constraint")
    void uniqueDocumentNumber() {
        jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-DOCUNIQ-H9").name("First")
                .documentType(DocumentType.CC).documentNumber("100000010")
                .email("docuniq.h9@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertThrows(Exception.class, () -> jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-DOCUNIQ2-H10").name("Second")
                .documentType(DocumentType.NIT).documentNumber("100000010")
                .email("docuniq2.h9@test.com")
                .status(CustomerStatus.ACTIVO)
                .build()));
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void uniqueEmail() {
        jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-EMAILUNIQ-H11").name("First")
                .documentType(DocumentType.CC).documentNumber("100000011")
                .email("unique-email.h11@test.com")
                .status(CustomerStatus.ACTIVO)
                .build());

        assertThrows(Exception.class, () -> jpaCustomerRepository.saveAndFlush(CustomerEntity.builder()
                .codigo("CLI-EMAILUNIQ2-H12").name("Second")
                .documentType(DocumentType.NIT).documentNumber("900200012")
                .email("unique-email.h11@test.com")
                .status(CustomerStatus.ACTIVO)
                .build()));
    }
}
