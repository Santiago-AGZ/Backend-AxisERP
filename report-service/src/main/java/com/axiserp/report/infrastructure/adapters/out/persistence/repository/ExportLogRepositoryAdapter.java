package com.axiserp.report.infrastructure.adapters.out.persistence.repository;

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import com.axiserp.report.ports.output.ExportLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExportLogRepositoryAdapter implements ExportLogRepositoryPort {

    private final JpaExportLogRepository jpaRepository;

    @Override
    public ExportLogEntity save(ExportLogEntity log) {
        return jpaRepository.save(log);
    }

    @Override
    public List<ExportLogEntity> findAllDesc() {
        return jpaRepository.findAllByOrderByGeneratedAtDesc();
    }

    @Override
    public List<ExportLogEntity> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByGeneratedAtBetweenOrderByGeneratedAtDesc(from, to);
    }

    @Override
    public List<ExportLogEntity> findByReportType(String reportType) {
        return jpaRepository.findByReportTypeOrderByGeneratedAtDesc(reportType);
    }
}
