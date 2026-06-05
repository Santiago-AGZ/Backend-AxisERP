package com.axiserp.report.infrastructure.adapters.out.persistence.repository;

import com.axiserp.report.domain.model.ExportLog;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import com.axiserp.report.ports.output.ExportLogRepositoryPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExportLogRepositoryAdapter implements ExportLogRepositoryPort {

    private final JpaExportLogRepository jpaRepository;

    @Override
    public ExportLog save(ExportLog log) {
        ExportLogEntity entity = toEntity(log);
        ExportLogEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ExportLog> findAllDesc() {
        return jpaRepository.findAllByOrderByGeneratedAtDesc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExportLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findByGeneratedAtBetweenOrderByGeneratedAtDesc(from, to).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExportLog> findByReportType(String reportType) {
        return jpaRepository.findByReportTypeOrderByGeneratedAtDesc(reportType).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ExportLog toDomain(ExportLogEntity e) {
        return ExportLog.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .reportType(e.getReportType())
                .format(e.getFormat())
                .recordCount(e.getRecordCount())
                .fileSizeBytes(e.getFileSizeBytes())
                .generatedAt(e.getGeneratedAt())
                .filterParams(e.getFilterParams())
                .build();
    }

    private ExportLogEntity toEntity(ExportLog d) {
        return ExportLogEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .reportType(d.getReportType())
                .format(d.getFormat())
                .recordCount(d.getRecordCount())
                .fileSizeBytes(d.getFileSizeBytes())
                .generatedAt(d.getGeneratedAt())
                .filterParams(d.getFilterParams())
                .build();
    }
}
