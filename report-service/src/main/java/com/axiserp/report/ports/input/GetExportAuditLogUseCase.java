package com.axiserp.report.ports.input;

import java.time.LocalDateTime;
import java.util.List;

import com.axiserp.report.domain.model.ExportLog;

public interface GetExportAuditLogUseCase {

    List<ExportLog> getAuditLog(LocalDateTime from, LocalDateTime to, String reportType);
}
