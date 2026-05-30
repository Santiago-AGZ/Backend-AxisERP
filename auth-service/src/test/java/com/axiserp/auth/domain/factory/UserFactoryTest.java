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
        User user = UserFactory.createNew(id, "Test", "test@axiserp.com", roleId, adminId);

        assertEquals(id, user.getId());
        assertEquals("Test", user.getName());
        assertEquals("test@axiserp.com", user.getEmail());
        assertEquals("", user.getPasswordHash());
        assertEquals(roleId, user.getRoleId());
        assertEquals(UserStatus.PENDIENTE, user.getStatus());
        assertEquals(adminId, user.getCreatedBy());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLastLoginAt());
        assertNull(user.getDeletedAt());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should update user preserving immutable fields")
    void update_success() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Old", "old@axiserp.com", roleId, adminId);
        UUID newRoleId = UUID.randomUUID();

        User updated = UserFactory.update(existing, "New Name", "new@axiserp.com", newRoleId);

        assertEquals("New Name", updated.getName());
        assertEquals("new@axiserp.com", updated.getEmail());
        assertEquals(newRoleId, updated.getRoleId());
        assertEquals("", updated.getPasswordHash());
        assertEquals(existing.getId(), updated.getId());
        assertEquals(existing.getCreatedBy(), updated.getCreatedBy());
        assertEquals(existing.getStatus(), updated.getStatus());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @DisplayName("Should deactivate user setting INACTIVO status and deletedAt")
    void deactivate_success() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", roleId, adminId);

        User deactivated = UserFactory.deactivate(existing);

        assertEquals(UserStatus.INACTIVO, deactivated.getStatus());
        assertNotNull(deactivated.getDeletedAt());
        assertEquals(existing.getId(), deactivated.getId());
        assertEquals(existing.getName(), deactivated.getName());
        assertEquals(existing.getPasswordHash(), deactivated.getPasswordHash());
    }

    @Test
    @DisplayName("Should update password and reset failed attempts")
    void withNewPassword_success() {
        User existing = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", roleId, adminId);

        User updated = UserFactory.withNewPassword(existing, "newHash");

        assertEquals("newHash", updated.getPasswordHash());
        assertEquals(0, updated.getFailedLoginAttempts());
        assertEquals(existing.getId(), updated.getId());
        assertEquals(existing.getEmail(), updated.getEmail());
    }

    @Test
    @DisplayName("Should increment failed attempts and lock at 5")
    void withFailedAttempt_locksAtFive() {
        User user = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", roleId, adminId);

        for (int i = 1; i <= 5; i++) {
            user = UserFactory.withFailedAttempt(user);
            assertEquals(i, user.getFailedLoginAttempts());
        }

        assertEquals(UserStatus.INACTIVO, user.getStatus());
    }

    @Test
    @DisplayName("Should not lock user before 5 attempts")
    void withFailedAttempt_notLockedBeforeFive() {
        User user = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", roleId, adminId);

        for (int i = 1; i <= 4; i++) {
            user = UserFactory.withFailedAttempt(user);
            assertEquals(UserStatus.PENDIENTE, user.getStatus());
        }
    }

    @Test
    @DisplayName("Should reset failed attempts on successful login")
    void withSuccessfulLogin_resetsAttempts() {
        User user = UserFactory.createNew(UUID.randomUUID(), "Test", "test@axiserp.com", roleId, adminId);
        user = UserFactory.withFailedAttempt(user);
        user = UserFactory.withFailedAttempt(user);
        assertEquals(2, user.getFailedLoginAttempts());

        User afterLogin = UserFactory.withSuccessfulLogin(user);

        assertEquals(0, afterLogin.getFailedLoginAttempts());
        assertNotNull(afterLogin.getLastLoginAt());
    }
}
