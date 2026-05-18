package com.axiserp.auth.ports.output;

import java.util.UUID;

import com.axiserp.auth.domain.model.PasswordResetToken;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken findByToken(String token);

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    void deleteByUserId(UUID userId);
}
