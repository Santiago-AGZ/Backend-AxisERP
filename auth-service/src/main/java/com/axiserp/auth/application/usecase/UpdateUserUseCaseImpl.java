package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.UpdateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.input.UpdateUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCaseImpl.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getStatus() == User.UserStatus.ELIMINADO) {
            throw new UserNotFoundException("Usuario no encontrado");
        }

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        var role = roleRepositoryPort.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.getRole()));

        User updated = UserFactory.update(user, request.getName(), request.getEmail(), role.getId());
        User saved = userRepositoryPort.save(updated);

        auditService.log(AuditAction.UPDATE, "USER", saved.getId(),
                null, null,
                java.util.Map.of("name", request.getName(), "email", request.getEmail(), "role", request.getRole()),
                null, null);

        log.info("user_updated id={} email={} role={}", saved.getId(), saved.getEmail(), request.getRole());

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(request.getRole())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
