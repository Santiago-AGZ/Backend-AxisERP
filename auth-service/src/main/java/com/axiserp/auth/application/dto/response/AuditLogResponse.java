package com.axiserp.auth.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String userName;
    private String action;
    private String entityType;
    private UUID entityId;
    private Map<String, Object> detail;
    private String ipAddress;
    private String userAgent;
}
