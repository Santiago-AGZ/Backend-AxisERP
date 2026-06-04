package com.axiserp.report.infrastructure.adapters.in.web.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;
import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.application.service.CsvExportService;
import com.axiserp.report.application.service.ExcelExportService;
import com.axiserp.report.application.service.PdfExportService;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.report.infrastructure.adapters.out.persistence.entity.ExportLogEntity;
import com.axiserp.report.ports.input.GenerateDashboardUseCase;
import com.axiserp.report.ports.input.GenerateFrequentCustomersReportUseCase;
import com.axiserp.report.ports.input.GenerateInventoryReportUseCase;
import com.axiserp.report.ports.input.GenerateSalesReportUseCase;
import com.axiserp.report.ports.input.GenerateTopProductsReportUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GenerateSalesReportUseCase generateSalesReportUseCase;
    private final GenerateInventoryReportUseCase generateInventoryReportUseCase;
    private final GenerateTopProductsReportUseCase generateTopProductsReportUseCase;
    private final GenerateDashboardUseCase generateDashboardUseCase;
    private final GenerateFrequentCustomersReportUseCase generateFrequentCustomersReportUseCase;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;
    private final ReportAuditService reportAuditService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID clientId) {
        return ResponseEntity.ok(ApiResponse.ok(
                generateSalesReportUseCase.execute(startDate, endDate, status, userId, clientId),
                "Reporte de ventas generado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryReportResponse>> getInventoryReport(
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(
                generateInventoryReportUseCase.execute(categoryId),
                "Reporte de inventario generado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<TopProductsReportResponse>> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                generateTopProductsReportUseCase.execute(startDate, endDate, limit),
                "Top productos generado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(
                generateDashboardUseCase.execute(),
                "Dashboard generado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales/export/pdf")
    public ResponseEntity<byte[]> exportSalesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID clientId) {
        byte[] pdf = pdfExportService.exportSalesReport(startDate, endDate, clientId);
        String filterParams = String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"clientId\":\"%s\"}",
                startDate, endDate, clientId);
        reportAuditService.logExport("DAILY_SALES", "PDF", null, (long) pdf.length, filterParams);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-ventas.pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/inventory/export/excel")
    public ResponseEntity<byte[]> exportInventoryExcel(
            @RequestParam(required = false) UUID categoryId) {
        byte[] excel = excelExportService.exportInventoryReport(categoryId);
        String filterParams = categoryId != null ? String.format("{\"categoryId\":\"%s\"}", categoryId) : "{}";
        reportAuditService.logExport("INVENTORY_STATUS", "EXCEL", null, (long) excel.length, filterParams);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "reporte-inventario.xlsx");
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/frequent")
    public ResponseEntity<ApiResponse<FrequentCustomerReportResponse>> getFrequentCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                generateFrequentCustomersReportUseCase.execute(startDate, endDate, limit),
                "Clientes frecuentes generado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales/export/csv")
    public ResponseEntity<byte[]> exportSalesCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID clientId) {
        byte[] csv = csvExportService.exportSalesReport(startDate, endDate, status, userId, clientId);
        String filterParams = String.format("{\"startDate\":\"%s\",\"endDate\":\"%s\",\"status\":\"%s\",\"userId\":\"%s\",\"clientId\":\"%s\"}",
                startDate, endDate, status, userId, clientId);
        reportAuditService.logExport("DAILY_SALES", "CSV", null, (long) csv.length, filterParams);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "reporte-ventas.csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<java.util.List<ExportLogEntity>>> getAuditLog(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime to,
            @RequestParam(required = false) String reportType) {
        var logs = reportAuditService.getAuditLog(from, to, reportType);
        return ResponseEntity.ok(ApiResponse.ok(logs, "Historial de exportaciones"));
    }
}
