package com.axiserp.auth.domain.service;

import org.springframework.stereotype.Service;

import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;

/**
 * Implementación de rate limiting basada en contador de intentos fallidos.
 * Bloquea al usuario tras 5 intentos consecutivos fallidos (RNF-001).
 */
@Service
public class FailedAttemptRateLimitStrategy implements LoginRateLimitStrategy {

    @Override
    public boolean isLoginAllowed(User user) {
        int attempts = user.getFailedLoginAttempts() != null
                ? user.getFailedLoginAttempts()
                : 0;
        return attempts < MAX_ATTEMPTS;
    }

    @Override
    public User recordFailedAttempt(User user) {
        return UserFactory.withFailedAttempt(user);
    }

    @Override
    public User recordSuccessfulLogin(User user) {
        return UserFactory.withSuccessfulLogin(user);
    }

    @Override
    public int remainingAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() != null
                ? user.getFailedLoginAttempts()
                : 0;
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }
}
