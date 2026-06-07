package com.axiserp.report.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.domain.model.ExportLog;
import com.axiserp.report.ports.input.GetExportAuditLogUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetExportAuditLogUseCaseImpl implements GetExportAuditLogUseCase {

    private final ReportAuditService reportAuditService;

    @Override
    public List<ExportLog> getAuditLog(LocalDateTime from, LocalDateTime to, String reportType) {
        return reportAuditService.getAuditLog(from, to, reportType);
    }
}
