package com.axiserp.auth.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String userName;
    private AuditAction action;
    private String entityType;
    private UUID entityId;
    private Map<String, Object> detail;
    private String ipAddress;
    private String userAgent;

    public enum AuditAction {
        LOGIN, LOGOUT,
        CREATE, UPDATE, DELETE, DEACTIVATE, REACTIVATE, VOID,
        PASSWORD_RESET_REQUEST, PASSWORD_RESET_COMPLETE
    }
}
