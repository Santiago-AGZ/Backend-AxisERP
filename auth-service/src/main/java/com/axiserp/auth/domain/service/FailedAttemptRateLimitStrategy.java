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
        return user.getFailedLoginAttempts() < MAX_ATTEMPTS;
    }

    @Override
    public User recordFailedAttempt(User user) {
        User updated = UserFactory.withFailedAttempt(user);
        if (updated.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
            return User.builder()
                    .id(updated.getId())
                    .name(updated.getName())
                    .email(updated.getEmail())
                    .passwordHash(updated.getPasswordHash())
                    .roleId(updated.getRoleId())
                    .status(User.UserStatus.INACTIVO)
                    .failedLoginAttempts(updated.getFailedLoginAttempts())
                    .createdBy(updated.getCreatedBy())
                    .updatedBy(updated.getUpdatedBy())
                    .lastLoginAt(updated.getLastLoginAt())
                    .createdAt(updated.getCreatedAt())
                    .updatedAt(updated.getUpdatedAt())
                    .deletedAt(updated.getDeletedAt())
                    .build();
        }
        return updated;
    }

    @Override
    public User recordSuccessfulLogin(User user) {
        return UserFactory.withSuccessfulLogin(user);
    }

    @Override
    public int remainingAttempts(User user) {
        return Math.max(0, MAX_ATTEMPTS - user.getFailedLoginAttempts());
    }
}
