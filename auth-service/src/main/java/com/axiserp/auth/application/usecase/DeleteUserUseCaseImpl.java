package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.ReauthenticationValidator;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.DeleteUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final ReauthenticationValidator reauthenticationValidator;

    @Override
    @Transactional
    public UserResponse delete(UUID id, UUID updatedBy, String currentPassword) {
        reauthenticationValidator.validate(updatedBy, currentPassword);
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getStatus() == User.UserStatus.ELIMINADO) {
            throw new IllegalStateException("El usuario ya está eliminado");
        }

        User deleted = UserFactory.logicalDelete(user, updatedBy);
        User saved = userRepositoryPort.save(deleted);

        refreshTokenService.revokeByUserId(saved.getId());

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(role -> role.getName())
                .orElse("UNKNOWN");

        auditService.log(AuditAction.DELETE, "USER", saved.getId(),
                null, null,
                java.util.Map.of("previousStatus", user.getStatus().name(), "newStatus", "ELIMINADO"),
                null, null);

        log.info("user_deleted id={} email={} previous_status={}", saved.getId(), saved.getEmail(), user.getStatus());

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
