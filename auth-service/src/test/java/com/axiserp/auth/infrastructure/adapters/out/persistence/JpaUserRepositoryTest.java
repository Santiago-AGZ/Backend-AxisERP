package com.axiserp.auth.infrastructure.adapters.out.persistence;

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

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RoleEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.UserEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.UserEntity.UserStatus;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRoleRepository;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaUserRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaUserRepositoryTest {

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
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    private UUID roleId;

    @BeforeEach
    void setUp() {
        jpaUserRepository.deleteAll();
        jpaRoleRepository.deleteAll();

        RoleEntity role = jpaRoleRepository.save(RoleEntity.builder()
                .name("ADMIN")
                .description("Administrador")
                .build());
        roleId = role.getId();
    }

    @Test
    @DisplayName("Should save and find user by email")
    void findByEmail() {
        UserEntity user = jpaUserRepository.save(UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@axiserp.com")
                .roleId(roleId)
                .status(UserStatus.ACTIVO)
                .build());

        var found = jpaUserRepository.findByEmail("test@axiserp.com");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void findByEmail_notFound() {
        var found = jpaUserRepository.findByEmail("noexiste@axiserp.com");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail() {
        jpaUserRepository.save(UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@axiserp.com")
                .roleId(roleId)
                .status(UserStatus.ACTIVO)
                .build());

        assertTrue(jpaUserRepository.existsByEmail("test@axiserp.com"));
        assertFalse(jpaUserRepository.existsByEmail("otro@axiserp.com"));
    }

    @Test
    @DisplayName("Should find by email or name")
    void findByEmailOrName() {
        jpaUserRepository.save(UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Juan Perez")
                .email("juan@axiserp.com")
                .roleId(roleId)
                .status(UserStatus.ACTIVO)
                .build());

        assertTrue(jpaUserRepository.findByEmailOrName("juan@axiserp.com", "x").isPresent());
        assertTrue(jpaUserRepository.findByEmailOrName("x", "Juan Perez").isPresent());
        assertTrue(jpaUserRepository.findByEmailOrName("x", "y").isEmpty());
    }

    @Test
    @DisplayName("Should filter by status")
    void findByFilters_status() {
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Admin").email("admin@axiserp.com").roleId(roleId).status(UserStatus.ACTIVO).build());
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Vendor").email("vendor@axiserp.com").roleId(roleId).status(UserStatus.INACTIVO).build());

        assertEquals(1, jpaUserRepository.findByFilters("ACTIVO", null).size());
        assertEquals(1, jpaUserRepository.findByFilters("INACTIVO", null).size());
    }

    @Test
    @DisplayName("Should filter by search term")
    void findByFilters_search() {
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Carlos Lopez").email("carlos@axiserp.com").roleId(roleId).status(UserStatus.ACTIVO).build());
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Maria Garcia").email("maria@axiserp.com").roleId(roleId).status(UserStatus.ACTIVO).build());

        assertEquals(1, jpaUserRepository.findByFilters(null, "carlos").size());
        assertEquals(1, jpaUserRepository.findByFilters(null, "maria@axiserp").size());
        assertEquals(2, jpaUserRepository.findByFilters(null, null).size());
    }

    @Test
    @DisplayName("Should combine status and search filters")
    void findByFilters_combined() {
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Active User").email("active@axiserp.com").roleId(roleId).status(UserStatus.ACTIVO).build());
        jpaUserRepository.save(UserEntity.builder().id(UUID.randomUUID()).name("Inactive User").email("inactive@axiserp.com").roleId(roleId).status(UserStatus.INACTIVO).build());

        assertEquals(1, jpaUserRepository.findByFilters("ACTIVO", "active").size());
        assertEquals(0, jpaUserRepository.findByFilters("ACTIVO", "inactive").size());
    }

    @Test
    @DisplayName("Should handle deleted users correctly")
    void softDelete() {
        UserEntity user = jpaUserRepository.save(UserEntity.builder()
                .id(UUID.randomUUID()).name("To Delete").email("delete@axiserp.com")
                .roleId(roleId).status(UserStatus.ACTIVO).build());

        assertTrue(jpaUserRepository.findById(user.getId()).isPresent());

        user.setStatus(UserStatus.ELIMINADO);
        jpaUserRepository.save(user);

        var deleted = jpaUserRepository.findById(user.getId());
        assertTrue(deleted.isPresent());
        assertEquals(UserStatus.ELIMINADO, deleted.get().getStatus());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void uniqueEmail() {
        jpaUserRepository.saveAndFlush(UserEntity.builder()
                .id(UUID.randomUUID()).name("First").email("same@axiserp.com")
                .roleId(roleId).status(UserStatus.ACTIVO).build());

        assertThrows(Exception.class, () -> jpaUserRepository.saveAndFlush(UserEntity.builder()
                .id(UUID.randomUUID()).name("Second").email("same@axiserp.com")
                .roleId(roleId).status(UserStatus.ACTIVO).build()));
    }
}
