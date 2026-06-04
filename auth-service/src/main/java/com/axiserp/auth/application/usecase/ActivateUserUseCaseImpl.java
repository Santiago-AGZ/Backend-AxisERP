package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.ActivateUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivateUserUseCaseImpl implements ActivateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserResponse activate(UUID id, UUID updatedBy) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getStatus() != User.UserStatus.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden activar usuarios en estado PENDIENTE");
        }

        User activated = UserFactory.activate(user, updatedBy);
        User saved = userRepositoryPort.save(activated);

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(role -> role.getName())
                .orElse("UNKNOWN");

        auditService.log(AuditAction.REACTIVATE, "USER", saved.getId(),
                null, null,
                java.util.Map.of("previousStatus", "PENDIENTE", "newStatus", "ACTIVO"),
                null, null);

        log.info("user_activated id={} email={}", saved.getId(), saved.getEmail());

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
