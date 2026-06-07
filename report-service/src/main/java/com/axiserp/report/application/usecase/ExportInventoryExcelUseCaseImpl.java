package com.axiserp.report.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.report.application.service.ExcelExportService;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.ports.input.ExportInventoryExcelUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportInventoryExcelUseCaseImpl implements ExportInventoryExcelUseCase {

    private final ExcelExportService excelExportService;
    private final ReportAuditService reportAuditService;

    @Override
    public byte[] exportInventoryExcel(UUID categoryId) {
        byte[] excel = excelExportService.exportInventoryReport(categoryId);
        String filterParams = categoryId != null ? String.format("{\"categoryId\":\"%s\"}", categoryId) : "{}";
        reportAuditService.logExport("INVENTORY_STATUS", "EXCEL", null, (long) excel.length, filterParams);
        return excel;
    }
}
