package com.axiserp.report.ports.output;

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ExportLogRepositoryPort {
    ExportLogEntity save(ExportLogEntity log);
    List<ExportLogEntity> findAllDesc();
    List<ExportLogEntity> findByDateRange(LocalDateTime from, LocalDateTime to);
    List<ExportLogEntity> findByReportType(String reportType);
}
