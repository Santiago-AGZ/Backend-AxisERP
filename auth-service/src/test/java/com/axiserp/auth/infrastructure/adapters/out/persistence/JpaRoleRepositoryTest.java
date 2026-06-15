package com.axiserp.auth.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

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

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RoleEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRoleRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRoleRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withInitScript("init-citext.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    @Test
    @DisplayName("Should save and find role by name")
    void findByName() {
        RoleEntity role = jpaRoleRepository.save(RoleEntity.builder()
                .name("ADMIN_ROLE")
                .description("Administrator role")
                .build());

        var found = jpaRoleRepository.findByName("ADMIN_ROLE");

        assertTrue(found.isPresent());
        assertEquals(role.getId(), found.get().getId());
        assertEquals("Administrator role", found.get().getDescription());
    }

    @Test
    @DisplayName("Should return empty when role name not found")
    void findByName_notFound() {
        var found = jpaRoleRepository.findByName("NONEXISTENT_ROLE");
        assertTrue(found.isEmpty());
    }
}
