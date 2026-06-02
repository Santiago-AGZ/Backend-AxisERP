package com.axiserp.auth.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;

class FailedAttemptRateLimitStrategyTest {

    private FailedAttemptRateLimitStrategy strategy;
    private UUID roleId = UUID.randomUUID();
    private UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        strategy = new FailedAttemptRateLimitStrategy();
    }

    @Test
    @DisplayName("Should allow login for user with no failed attempts")
    void isLoginAllowed_noAttempts() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);

        assertTrue(strategy.isLoginAllowed(user));
        assertEquals(5, strategy.remainingAttempts(user));
    }

    @Test
    @DisplayName("Should allow login for user with some failed attempts")
    void isLoginAllowed_someAttempts() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);
        user = strategy.recordFailedAttempt(user);
        user = strategy.recordFailedAttempt(user);

        assertTrue(strategy.isLoginAllowed(user));
        assertEquals(3, strategy.remainingAttempts(user));
    }

    @Test
    @DisplayName("Should block login after 5 failed attempts")
    void isLoginAllowed_blockedAfterFive() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);

        for (int i = 0; i < 5; i++) {
            user = strategy.recordFailedAttempt(user);
        }

        assertFalse(strategy.isLoginAllowed(user));
        assertEquals(0, strategy.remainingAttempts(user));
        assertEquals(UserStatus.INACTIVO, user.getStatus());
    }

    @Test
    @DisplayName("Should increment failed attempts on each failure")
    void recordFailedAttempt_increments() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);

        user = strategy.recordFailedAttempt(user);
        assertEquals(1, user.getFailedLoginAttempts());

        user = strategy.recordFailedAttempt(user);
        assertEquals(2, user.getFailedLoginAttempts());
    }

    @Test
    @DisplayName("Should reset attempts on successful login")
    void recordSuccessfulLogin_resets() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);
        user = strategy.recordFailedAttempt(user);
        user = strategy.recordFailedAttempt(user);
        assertEquals(2, user.getFailedLoginAttempts());

        User afterLogin = strategy.recordSuccessfulLogin(user);

        assertEquals(0, afterLogin.getFailedLoginAttempts());
        assertNotNull(afterLogin.getLastLoginAt());
    }

    @Test
    @DisplayName("Should return correct remaining attempts")
    void remainingAttempts_correct() {
        User user = UserFactory.createNew("Test", "test@axiserp.com", "hashed", roleId, adminId);

        assertEquals(5, strategy.remainingAttempts(user));

        user = strategy.recordFailedAttempt(user);
        assertEquals(4, strategy.remainingAttempts(user));

        user = strategy.recordFailedAttempt(user);
        assertEquals(3, strategy.remainingAttempts(user));
    }
}
