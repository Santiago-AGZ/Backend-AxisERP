package com.axiserp.auth.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.auth.application.dto.response.AuditLogResponse;

public interface GetAuditLogUseCase {

    List<AuditLogResponse> getAuditLogs(UUID userId, String action, String entityType, int page, int size);
}
