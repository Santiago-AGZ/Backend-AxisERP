package com.axiserp.auth.domain.factory;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;

public class UserFactory {

    private UserFactory() {
    }

    public static User createNew(UUID id, String name, String email, String passwordHash,
                                   UUID roleId, UUID createdBy) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .roleId(roleId)
                .status(UserStatus.PENDIENTE)
                .failedLoginAttempts(0)
                .createdBy(createdBy)
                .lastLoginAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static User update(User existing, String name, String email, UUID roleId, UUID updatedBy) {
        return User.builder()
                .id(existing.getId())
                .name(name)
                .email(email)
                .roleId(roleId)
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    public static User reactivate(User existing, UUID updatedBy) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .roleId(existing.getRoleId())
                .status(UserStatus.ACTIVO)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
    }

    public static User activate(User existing, UUID updatedBy) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .roleId(existing.getRoleId())
                .status(UserStatus.ACTIVO)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    public static User deactivate(User existing, UUID updatedBy) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .roleId(existing.getRoleId())
                .status(UserStatus.INACTIVO)
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    public static User logicalDelete(User existing, UUID updatedBy) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .roleId(existing.getRoleId())
                .status(UserStatus.ELIMINADO)
                .failedLoginAttempts(existing.getFailedLoginAttempts())
                .createdBy(existing.getCreatedBy())
                .updatedBy(updatedBy)
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    public static User withFailedAttempt(User existing) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(existing.getPasswordHash())
                .roleId(existing.getRoleId())
                .status(existing.getStatus())
                .failedLoginAttempts(existing.getFailedLoginAttempts() + 1)
                .createdBy(existing.getCreatedBy())
                .updatedBy(existing.getUpdatedBy())
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    public static User withNewPassword(User existing, String newPasswordHash) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(newPasswordHash)
                .roleId(existing.getRoleId())
                .status(existing.getStatus())
                .failedLoginAttempts(existing.getFailedLoginAttempts())
                .createdBy(existing.getCreatedBy())
                .updatedBy(existing.getId())
                .lastLoginAt(existing.getLastLoginAt())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    public static User withSuccessfulLogin(User existing) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .roleId(existing.getRoleId())
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .updatedBy(existing.getId())
                .lastLoginAt(LocalDateTime.now())
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .deletedAt(existing.getDeletedAt())
                .build();
    }
}
