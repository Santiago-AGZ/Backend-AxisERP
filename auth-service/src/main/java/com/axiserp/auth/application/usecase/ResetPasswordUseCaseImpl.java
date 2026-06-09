package com.axiserp.auth.application.usecase;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.TokenExpiredException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.PasswordHistory;
import com.axiserp.auth.domain.model.PasswordResetToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.service.PasswordValidator;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;
import com.axiserp.auth.ports.output.PasswordHistoryRepositoryPort;
import com.axiserp.auth.ports.output.PasswordResetTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordUseCaseImpl.class);

    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final PasswordHistoryRepositoryPort passwordHistoryRepositoryPort;

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepositoryPort.findByToken(token);

        if (resetToken == null) {
            throw new TokenExpiredException("Token de recuperación inválido");
        }

        if (!resetToken.isValid()) {
            if (resetToken.isUsed()) {
                throw new TokenExpiredException("El enlace ya fue utilizado");
            }
            throw new TokenExpiredException("El enlace ha expirado");
        }

        User user = userRepositoryPort.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        PasswordValidator.validate(newPassword);

        var recentPasswords = passwordHistoryRepositoryPort.findLastByUserId(user.getId(), 5);
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new IllegalArgumentException("La contraseña ya fue utilizada recientemente");
            }
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        User updated = UserFactory.withNewPassword(user, hashedPassword);
        userRepositoryPort.save(updated);

        PasswordResetToken usedToken = PasswordResetToken.builder()
                .id(resetToken.getId())
                .userId(resetToken.getUserId())
                .token(resetToken.getToken())
                .used(true)
                .expiresAt(resetToken.getExpiresAt())
                .createdAt(resetToken.getCreatedAt())
                .usedAt(LocalDateTime.now(ZoneOffset.UTC))
                .ipAddress(resetToken.getIpAddress())
                .build();
        passwordResetTokenRepositoryPort.save(usedToken);

        passwordHistoryRepositoryPort.save(PasswordHistory.builder()
                .userId(user.getId())
                .passwordHash(hashedPassword)
                .build());

        auditService.log(AuditAction.PASSWORD_RESET_COMPLETE, "AUTH", user.getId(),
                user.getId(), user.getName(),
                java.util.Map.of("email", user.getEmail()),
                null, null);

        log.info("password_reset_complete user_id={}", user.getId());
    }
}
