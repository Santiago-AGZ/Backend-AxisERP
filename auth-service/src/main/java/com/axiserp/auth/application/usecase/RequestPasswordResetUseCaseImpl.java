package com.axiserp.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.PasswordResetToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.RequestPasswordResetUseCase;
import com.axiserp.auth.ports.output.PasswordResetTokenRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestPasswordResetUseCaseImpl implements RequestPasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;
    private final SupabaseAuthPort supabaseAuthPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public void requestReset(String email) {
        var userOpt = userRepositoryPort.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.info("password_reset_requested_unknown_email email={}", email);
            return;
        }

        User user = userOpt.get();

        passwordResetTokenRepositoryPort.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        passwordResetTokenRepositoryPort.save(resetToken);

        supabaseAuthPort.sendPasswordReset(user.getEmail());

        auditService.log(AuditAction.PASSWORD_RESET_REQUEST, "AUTH", user.getId(),
                user.getId(), user.getName(),
                java.util.Map.of("email", email),
                null, null);

        log.info("password_reset_requested user_id={} email={}", user.getId(), email);
    }
}
