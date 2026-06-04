package com.axiserp.report.application.service;

import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import com.axiserp.report.ports.output.ExportLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportAuditService {

    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final ExportLogRepositoryPort exportLogRepository;

    public void logReportGeneration(String reportType, String format, Integer recordCount, String filterParams) {
        ExportLogEntity log = ExportLogEntity.builder()
                .userId(getCurrentUserId())
                .reportType(reportType)
                .format(format)
                .recordCount(recordCount)
                .generatedAt(LocalDateTime.now())
                .filterParams(filterParams)
                .build();
        exportLogRepository.save(log);
    }

    public void logExport(String reportType, String format, Integer recordCount, Long fileSizeBytes, String filterParams) {
        ExportLogEntity log = ExportLogEntity.builder()
                .userId(getCurrentUserId())
                .reportType(reportType)
                .format(format)
                .recordCount(recordCount)
                .fileSizeBytes(fileSizeBytes)
                .generatedAt(LocalDateTime.now())
                .filterParams(filterParams)
                .build();
        exportLogRepository.save(log);
    }

    public List<ExportLogEntity> getAuditLog(LocalDateTime from, LocalDateTime to, String reportType) {
        if (reportType != null && !reportType.isBlank()) {
            return exportLogRepository.findByReportType(reportType);
        }
        if (from != null && to != null) {
            return exportLogRepository.findByDateRange(from, to);
        }
        return exportLogRepository.findAllDesc();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            try {
                return UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                // invalid UUID format, fall through
            }
        }
        return SYSTEM_USER_ID;
    }
}
