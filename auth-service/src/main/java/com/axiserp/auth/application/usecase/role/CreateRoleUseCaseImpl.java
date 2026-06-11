package com.axiserp.auth.application.usecase.role;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.CreateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.ports.input.CreateRoleUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateRoleUseCaseImpl implements CreateRoleUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateRoleUseCaseImpl.class);

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest request, UUID createdBy) {
        if (roleRepositoryPort.existsByName(request.getName())) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre: " + request.getName());
        }

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        Role saved = roleRepositoryPort.save(role);

        log.info("role_created id={} name={} created_by={}", saved.getId(), saved.getName(), createdBy);

        return RoleResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
