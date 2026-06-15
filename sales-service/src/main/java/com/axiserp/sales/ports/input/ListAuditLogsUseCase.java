package com.axiserp.sales.ports.input;

import java.util.List;

import com.axiserp.sales.application.dto.response.AuditLogResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;

public interface ListAuditLogsUseCase {
    PaginatedResponse<AuditLogResponse> list(int page, int size);
}
