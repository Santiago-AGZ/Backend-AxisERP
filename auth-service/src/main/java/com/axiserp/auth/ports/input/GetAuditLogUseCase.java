package com.axiserp.auth.ports.input;

import java.util.UUID;

import com.axiserp.auth.application.dto.response.AuditLogResponse;
import com.axiserp.auth.domain.model.PageResult;

public interface GetAuditLogUseCase {

    PageResult<AuditLogResponse> getAuditLogs(UUID userId, String action, String entityType, int page, int size);
}
