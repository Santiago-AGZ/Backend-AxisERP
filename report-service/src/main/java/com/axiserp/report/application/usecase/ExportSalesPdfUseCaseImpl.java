package com.axiserp.report.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.report.application.service.PdfExportService;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.ports.input.ExportSalesPdfUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportSalesPdfUseCaseImpl implements ExportSalesPdfUseCase {

    private final PdfExportService pdfExportService;
    private final ReportAuditService reportAuditService;

    @Override
    public byte[] exportSalesPdf(LocalDate startDate, LocalDate endDate, UUID clientId) {
        byte[] pdf = pdfExportService.exportSalesReport(startDate, endDate, clientId);
        String filterParams = String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"clientId\":\"%s\"}",
                startDate, endDate, clientId);
        reportAuditService.logExport("DAILY_SALES", "PDF", null, (long) pdf.length, filterParams);
        return pdf;
    }
}
