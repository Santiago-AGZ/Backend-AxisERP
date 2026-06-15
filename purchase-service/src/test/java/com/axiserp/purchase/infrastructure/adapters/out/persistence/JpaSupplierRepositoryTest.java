package com.axiserp.purchase.infrastructure.adapters.out.persistence;

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

import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.SupplierEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.SupplierEntity.SupplierStatus;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaSupplierRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaSupplierRepositoryTest {

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
    private JpaSupplierRepository jpaSupplierRepository;

    @BeforeEach
    void setUp() {
        jpaSupplierRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find supplier by codigo")
    void findByCodigo() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID())
                .codigo("SUP-F1")
                .name("Proveedor Uno F1")
                .nit("900123456-1")
                .status(SupplierStatus.ACTIVO)
                .build());

        var found = jpaSupplierRepository.findByCodigo("SUP-F1");
        assertTrue(found.isPresent());
        assertEquals("Proveedor Uno F1", found.get().getName());
    }

    @Test
    @DisplayName("Should check existence by codigo")
    void existsByCodigo() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-EXISTS-F2")
                .name("Exists Test").nit("900123456-2")
                .status(SupplierStatus.ACTIVO).build());

        assertTrue(jpaSupplierRepository.existsByCodigo("SUP-EXISTS-F2"));
        assertFalse(jpaSupplierRepository.existsByCodigo("NONEXISTENT"));
    }

    @Test
    @DisplayName("Should check existence by nit")
    void existsByNit() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-NIT-F3")
                .name("Nit Test").nit("900123456-3")
                .status(SupplierStatus.ACTIVO).build());

        assertTrue(jpaSupplierRepository.existsByNit("900123456-3"));
        assertFalse(jpaSupplierRepository.existsByNit("000000000-0"));
    }

    @Test
    @DisplayName("Should check existence by nit excluding id")
    void existsByNitAndIdNot() {
        SupplierEntity saved = jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-NIT-EXCL-F4")
                .name("Nit Exclude Test").nit("900123456-4")
                .status(SupplierStatus.ACTIVO).build());

        assertFalse(jpaSupplierRepository.existsByNitAndIdNot("900123456-4", saved.getId()));
        assertTrue(jpaSupplierRepository.existsByNitAndIdNot("900123456-4", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should find by nit")
    void findByNit() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-FINDNIT-F5")
                .name("Find Nit Test").nit("900123456-5")
                .status(SupplierStatus.ACTIVO).build());

        var found = jpaSupplierRepository.findByNit("900123456-5");
        assertTrue(found.isPresent());
        assertEquals("Find Nit Test", found.get().getName());
    }

    @Test
    @DisplayName("Should find by status ordered by name")
    void findByStatusOrderByNameAsc() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-Z-F6").name("Zeta Supplier")
                .nit("900123456-6").status(SupplierStatus.ACTIVO).build());
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-A-F7").name("Alpha Supplier")
                .nit("900123456-7").status(SupplierStatus.ACTIVO).build());

        var active = jpaSupplierRepository.findByStatusOrderByNameAsc(SupplierStatus.ACTIVO);
        assertEquals(2, active.size());
        assertEquals("Alpha Supplier", active.get(0).getName());
    }

    @Test
    @DisplayName("Should count by status")
    void countByStatus() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-ACT-F8").name("Active Supplier")
                .nit("900123456-8").status(SupplierStatus.ACTIVO).build());
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-INACT-F9").name("Inactive Supplier")
                .nit("900123456-9").status(SupplierStatus.INACTIVO).build());

        assertEquals(1, jpaSupplierRepository.countByStatus(SupplierStatus.ACTIVO));
        assertEquals(1, jpaSupplierRepository.countByStatus(SupplierStatus.INACTIVO));
    }

    @Test
    @DisplayName("Should find suppliers by search term across name, codigo, nit")
    void findBySearch() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-BUS-F10").name("Busqueda Proveedor")
                .nit("900123456-10").status(SupplierStatus.ACTIVO).build());
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-OTHER-F11").name("Other Company")
                .nit("900123456-11").status(SupplierStatus.ACTIVO).build());

        var byName = jpaSupplierRepository.findBySearch("Busqueda", 10, 0);
        assertEquals(1, byName.size());

        var byCodigo = jpaSupplierRepository.findBySearch("SUP-BUS", 10, 0);
        assertEquals(1, byCodigo.size());
    }

    @Test
    @DisplayName("Should count suppliers by search")
    void countBySearch() {
        jpaSupplierRepository.save(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-COUNT-F12").name("Count Supplier")
                .nit("900123456-12").status(SupplierStatus.ACTIVO).build());

        assertEquals(1, jpaSupplierRepository.countBySearch("Count"));
        assertEquals(1, jpaSupplierRepository.countBySearch(null));
    }

    @Test
    @DisplayName("Should enforce unique codigo constraint")
    void uniqueCodigo() {
        jpaSupplierRepository.saveAndFlush(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-UNIQ-F13").name("First").nit("900123456-13")
                .status(SupplierStatus.ACTIVO).build());

        assertThrows(Exception.class, () -> jpaSupplierRepository.saveAndFlush(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-UNIQ-F13").name("Second").nit("900123456-14")
                .status(SupplierStatus.ACTIVO).build()));
    }

    @Test
    @DisplayName("Should enforce unique nit constraint")
    void uniqueNit() {
        jpaSupplierRepository.saveAndFlush(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-NITUNIQ-F14").name("First").nit("900123456-15")
                .status(SupplierStatus.ACTIVO).build());

        assertThrows(Exception.class, () -> jpaSupplierRepository.saveAndFlush(SupplierEntity.builder()
                .id(UUID.randomUUID()).codigo("SUP-NITUNIQ-F15").name("Second").nit("900123456-15")
                .status(SupplierStatus.ACTIVO).build()));
    }
}
