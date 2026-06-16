package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private UUID roleId;
    private UserStatus status;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return (this.status == UserStatus.ACTIVO || this.status == UserStatus.PENDIENTE) && this.deletedAt == null;
    }

    public enum UserStatus {
        PENDIENTE, ACTIVO, INACTIVO, ELIMINADO
    }
}
