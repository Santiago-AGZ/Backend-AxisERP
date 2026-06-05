package com.axiserp.report.ports.output;

import com.axiserp.report.domain.model.ExportLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ExportLogRepositoryPort {
    ExportLog save(ExportLog log);
    List<ExportLog> findAllDesc();
    List<ExportLog> findByDateRange(LocalDateTime from, LocalDateTime to);
    List<ExportLog> findByReportType(String reportType);
}
