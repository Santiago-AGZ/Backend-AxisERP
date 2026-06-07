package com.axiserp.report.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.report.application.service.CsvExportService;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.ports.input.ExportSalesCsvUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportSalesCsvUseCaseImpl implements ExportSalesCsvUseCase {

    private final CsvExportService csvExportService;
    private final ReportAuditService reportAuditService;

    @Override
    public byte[] exportSalesCsv(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId) {
        byte[] csv = csvExportService.exportSalesReport(startDate, endDate, status, userId, clientId);
        String filterParams = String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"status\":\"%s\",\"userId\":\"%s\",\"clientId\":\"%s\"}",
                startDate, endDate, status, userId, clientId);
        reportAuditService.logExport("DAILY_SALES", "CSV", null, (long) csv.length, filterParams);
        return csv;
    }
}
