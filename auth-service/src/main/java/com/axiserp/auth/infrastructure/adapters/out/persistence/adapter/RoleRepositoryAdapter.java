package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = toEntity(role);
        RoleEntity saved = jpaRoleRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRoleRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRoleRepository.findByName(name).isPresent();
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

    private RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        return entity;
    }
}
