package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.RoleEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaRoleRepository;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRoleRepository;

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRoleRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRoleRepository.findByName(name).map(this::toDomain);
    }

    private Role toDomain(RoleEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
