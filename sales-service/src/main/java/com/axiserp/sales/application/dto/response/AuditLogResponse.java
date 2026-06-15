package com.axiserp.sales.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    String action,
    String entityType,
    UUID entityId,
    String details,
    UUID userId,
    String userName,
    String ipAddress,
    String userAgent,
    LocalDateTime timestamp
) {}
