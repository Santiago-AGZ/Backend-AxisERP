package com.axiserp.auth.application.usecase.role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.UpdateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.ports.input.UpdateRoleUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateRoleUseCaseImpl implements UpdateRoleUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateRoleUseCaseImpl.class);

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional
    public RoleResponse update(UUID id, UpdateRoleRequest request, UUID updatedBy) {
        var existing = roleRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Rol no encontrado: " + id));

        var updated = com.axiserp.auth.domain.model.Role.builder()
                .id(existing.getId())
                .name(request.getName() != null ? request.getName() : existing.getName())
                .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        var saved = roleRepositoryPort.save(updated);

        log.info("role_updated id={} name={} updated_by={}", saved.getId(), saved.getName(), updatedBy);

        return RoleResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
