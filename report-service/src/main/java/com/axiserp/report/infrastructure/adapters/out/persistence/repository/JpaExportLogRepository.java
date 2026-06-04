package com.axiserp.report.infrastructure.adapters.out.persistence.repository;

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaExportLogRepository extends JpaRepository<ExportLogEntity, UUID> {
    List<ExportLogEntity> findAllByOrderByGeneratedAtDesc();
    List<ExportLogEntity> findByGeneratedAtBetweenOrderByGeneratedAtDesc(LocalDateTime from, LocalDateTime to);
    List<ExportLogEntity> findByReportTypeOrderByGeneratedAtDesc(String reportType);
}
