package com.axiserp.auth.application.usecase.role;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.ports.input.GetRoleUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetRoleUseCaseImpl implements GetRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(UUID id) {
        var role = roleRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Rol no encontrado: " + id));
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
