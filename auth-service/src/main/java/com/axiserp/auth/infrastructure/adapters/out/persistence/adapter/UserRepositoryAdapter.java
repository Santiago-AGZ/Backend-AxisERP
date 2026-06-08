package com.axiserp.auth.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.UserEntity;
import com.axiserp.auth.infrastructure.adapters.out.persistence.repository.JpaUserRepository;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmailOrName(String identifier) {
        return jpaUserRepository.findByEmailOrName(identifier, identifier).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaUserRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<User> findAll(String status, String search) {
        return jpaUserRepository.findByFilters(status, search).stream()
                .map(this::toDomain)
                .toList();
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .roleId(entity.getRoleId())
                .status(User.UserStatus.valueOf(entity.getStatus().name()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .failedLoginAttempts(entity.getFailedLoginAttempts())
                .build();
    }

    private UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .roleId(domain.getRoleId())
                .status(UserEntity.UserStatus.valueOf(domain.getStatus().name()))
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .lastLoginAt(domain.getLastLoginAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .failedLoginAttempts(domain.getFailedLoginAttempts())
                .build();
    }
}
