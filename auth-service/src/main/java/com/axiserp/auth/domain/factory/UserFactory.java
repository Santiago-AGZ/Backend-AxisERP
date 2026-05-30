package com.axiserp.auth.domain.factory;

import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;

/**
 * Factory para creación y modificación de entidades User.
 * Centraliza la lógica de construcción para evitar código repetitivo
 * en los casos de uso (Patrón Factory).
 */
public class UserFactory {

    private UserFactory() {
    }

    /**
     * Crea un nuevo usuario con valores por defecto.
     * Autenticación delegada a Supabase Auth; no se almacena password hash local.
     */
    public static User createNew(UUID id, String name, String email,
                                  UUID roleId, UUID createdBy) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .passwordHash("")
                .roleId(roleId)
                .status(UserStatus.PENDIENTE)
                .createdBy(createdBy)
                .failedLoginAttempts(0)
                .lastLoginAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
    }

    /**
     * Actualiza los datos de un usuario existente.
     */
    public static User update(User existing, String name, String email, UUID roleId) {
        return User.builder()
                .id(existing.getId())
                .name(name)
                .email(email)
                .passwordHash(existing.getPasswordHash())
                .roleId(roleId)
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .lastLoginAt(existing.getLastLoginAt())
                .failedLoginAttempts(existing.getFailedLoginAttempts())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    /**
     * Desactiva un usuario (soft delete).
     */
    public static User deactivate(User existing) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(existing.getPasswordHash())
                .roleId(existing.getRoleId())
                .status(UserStatus.INACTIVO)
                .createdBy(existing.getCreatedBy())
                .lastLoginAt(existing.getLastLoginAt())
                .failedLoginAttempts(existing.getFailedLoginAttempts())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Actualiza el password de un usuario.
     */
    public static User withNewPassword(User existing, String hashedPassword) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(hashedPassword)
                .roleId(existing.getRoleId())
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .lastLoginAt(existing.getLastLoginAt())
                .failedLoginAttempts(0)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    /**
     * Incrementa los intentos fallidos de login.
     */
    public static User withFailedAttempt(User existing) {
        int newAttempts = existing.getFailedLoginAttempts() != null
                ? existing.getFailedLoginAttempts() + 1
                : 1;
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(existing.getPasswordHash())
                .roleId(existing.getRoleId())
                .status(newAttempts >= 5 ? UserStatus.INACTIVO : existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .lastLoginAt(existing.getLastLoginAt())
                .failedLoginAttempts(newAttempts)
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();
    }

    /**
     * Resetea los intentos fallidos tras login exitoso.
     */
    public static User withSuccessfulLogin(User existing) {
        return User.builder()
                .id(existing.getId())
                .name(existing.getName())
                .email(existing.getEmail())
                .passwordHash(existing.getPasswordHash())
                .roleId(existing.getRoleId())
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .lastLoginAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .createdAt(existing.getCreatedAt())
                .updatedAt(existing.getUpdatedAt())
                .deletedAt(existing.getDeletedAt())
                .build();
    }
}
