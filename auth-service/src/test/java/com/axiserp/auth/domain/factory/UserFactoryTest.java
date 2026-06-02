package com.axiserp.auth.domain.factory;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;

class UserFactoryTest {

    private UUID id = UUID.randomUUID();
    private UUID roleId = UUID.randomUUID();
    private UUID adminId = UUID.randomUUID();

    @Test
    @DisplayName("Should create new user with default values")
    void createNew_success() {
        User user = UserFactory.createNew(id, "Test", "test@axiserp.com", "hashed", roleId, adminId);

        assertEquals(id, user.getId());
        assertEquals("Test", user.getName());
        assertEquals("test@axiserp.com", user.getEmail());
        assertEquals(roleId, user.getRoleId());
        assertEquals(UserStatus.PENDIENTE, user.getStatus());
        assertEquals(adminId, user.getCreatedBy());
        assertNull(user.getLastLoginAt());
        assertNull(user.getDeletedAt());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update user preserving immutable fields")
    void update_success() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Old", "old@axiserp.com", "hashed", roleId, adminId);
        UUID newRoleId = UUID.randomUUID();

        User updated = UserFactory.update(existing, "New Name", "new@axiserp.com", newRoleId, adminId);

        assertEquals("New Name", updated.getName());
        assertEquals("new@axiserp.com", updated.getEmail());
        assertEquals(newRoleId, updated.getRoleId());
        assertEquals(existing.getId(), updated.getId());
        assertEquals(existing.getCreatedBy(), updated.getCreatedBy());
        assertEquals(adminId, updated.getUpdatedBy());
        assertEquals(existing.getStatus(), updated.getStatus());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @DisplayName("Should deactivate user setting INACTIVO status and deletedAt")
    void deactivate_success() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", "hashed", roleId, adminId);

        User deactivated = UserFactory.deactivate(existing, adminId);

        assertEquals(UserStatus.INACTIVO, deactivated.getStatus());
        assertNotNull(deactivated.getDeletedAt());
        assertEquals(existing.getId(), deactivated.getId());
        assertEquals(existing.getName(), deactivated.getName());
        assertEquals(adminId, deactivated.getUpdatedBy());
    }

    @Test
    @DisplayName("Should update lastLoginAt on successful login")
    void withSuccessfulLogin_updatesLastLogin() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", "hashed", roleId, adminId);

        User afterLogin = UserFactory.withSuccessfulLogin(existing);

        assertNotNull(afterLogin.getLastLoginAt());
        assertEquals(existing.getId(), afterLogin.getUpdatedBy());
        assertEquals(existing.getId(), afterLogin.getId());
        assertEquals(existing.getStatus(), afterLogin.getStatus());
    }
}
