package com.axiserp.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.DeactivateUserUseCase;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeactivateUserUseCaseImpl implements DeactivateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserResponse deactivate(UUID id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getStatus() == User.UserStatus.INACTIVO) {
            throw new IllegalStateException("El usuario ya está desactivado");
        }

        User deactivated = UserFactory.deactivate(user);
        User saved = userRepositoryPort.save(deactivated);

        List<RefreshToken> activeTokens = refreshTokenRepositoryPort.findActiveByUserId(id);

        for (RefreshToken token : activeTokens) {
            token.setStatus(RefreshToken.TokenStatus.REVOKED);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepositoryPort.save(token);

            TokenBlacklist blacklist = TokenBlacklist.builder()
                    .token(token.getToken())
                    .tokenType("refresh")
                    .userId(id)
                    .reason("USER_DEACTIVATED")
                    .expiresAt(token.getExpiresAt())
                    .build();
            tokenBlacklistRepositoryPort.save(blacklist);
        }

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(role -> role.getName())
                .orElse("UNKNOWN");

        auditService.log(AuditAction.DEACTIVATE, "USER", saved.getId(),
                null, null,
                java.util.Map.of("previousStatus", "ACTIVO", "newStatus", "INACTIVO"),
                null, null);

        log.info("user_deactivated id={} email={}", saved.getId(), saved.getEmail());

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(roleName)
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
