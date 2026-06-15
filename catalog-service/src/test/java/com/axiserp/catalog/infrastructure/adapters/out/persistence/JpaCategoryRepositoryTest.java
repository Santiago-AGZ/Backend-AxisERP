package com.axiserp.catalog.infrastructure.adapters.out.persistence;

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

import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CategoryEntity;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.entity.CategoryEntity.CategoryStatus;
import com.axiserp.catalog.infrastructure.adapters.out.persistence.repository.JpaCategoryRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCategoryRepositoryTest {

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
    private JpaCategoryRepository jpaCategoryRepository;

    @BeforeEach
    void setUp() {
        jpaCategoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find category by name")
    void findByName() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Electronicos C1")
                .description("Electronic items")
                .status(CategoryStatus.ACTIVA)
                .build());

        var found = jpaCategoryRepository.findByName("Electronicos C1");
        assertTrue(found.isPresent());
        assertEquals("Electronic items", found.get().getDescription());
    }

    @Test
    @DisplayName("Should return true when category name exists")
    void existsByName() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Ropa C2")
                .status(CategoryStatus.ACTIVA)
                .build());

        assertTrue(jpaCategoryRepository.existsByName("Ropa C2"));
        assertFalse(jpaCategoryRepository.existsByName("NONEXISTENT CATEGORY"));
    }

    @Test
    @DisplayName("Should find all categories ordered by name asc")
    void findAllByOrderByNameAsc() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Zapatos C3").status(CategoryStatus.ACTIVA).build());
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Accesorios C4").status(CategoryStatus.ACTIVA).build());

        var all = jpaCategoryRepository.findAllByOrderByNameAsc();
        assertEquals(2, all.size());
        assertTrue(all.get(0).getName().compareTo(all.get(1).getName()) <= 0);
    }

    @Test
    @DisplayName("Should find by status ordered by status and name")
    void findByStatusOrderByStatusAscNameAsc() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Z C5").status(CategoryStatus.ACTIVA).build());
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("A C6").status(CategoryStatus.INACTIVA).build());

        var active = jpaCategoryRepository.findByStatusOrderByStatusAscNameAsc(CategoryStatus.ACTIVA);
        assertEquals(1, active.size());
        assertEquals("Z C5", active.get(0).getName());
    }

    @Test
    @DisplayName("Should find categories by filters")
    void findByFilters() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Busqueda C7").status(CategoryStatus.ACTIVA).build());
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Otra C8").status(CategoryStatus.ACTIVA).build());

        var bySearch = jpaCategoryRepository.findByFilters("Busqueda", true, 10, 0);
        assertEquals(1, bySearch.size());

        var all = jpaCategoryRepository.findByFilters(null, true, 10, 0);
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should count categories by filters")
    void countByFilters() {
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Count Me C9").status(CategoryStatus.ACTIVA).build());
        jpaCategoryRepository.save(CategoryEntity.builder()
                .id(UUID.randomUUID()).name("Count Too C10").status(CategoryStatus.ACTIVA).build());

        long total = jpaCategoryRepository.countByFilters(null, true);
        assertEquals(2, total);
    }

    @Test
    @DisplayName("Should enforce unique name constraint")
    void uniqueName() {
        String name = "Unique Name C11";
        jpaCategoryRepository.saveAndFlush(CategoryEntity.builder()
                .id(UUID.randomUUID()).name(name).status(CategoryStatus.ACTIVA).build());

        assertThrows(Exception.class, () -> jpaCategoryRepository.saveAndFlush(CategoryEntity.builder()
                .id(UUID.randomUUID()).name(name).status(CategoryStatus.ACTIVA).build()));
    }
}
