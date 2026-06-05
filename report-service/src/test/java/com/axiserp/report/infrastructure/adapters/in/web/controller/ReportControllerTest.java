package com.axiserp.report.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;
import com.axiserp.report.application.service.CsvExportService;
import com.axiserp.report.application.service.ExcelExportService;
import com.axiserp.report.application.service.PdfExportService;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.domain.model.ExportLog;
import com.axiserp.report.ports.input.GenerateDashboardUseCase;
import com.axiserp.report.ports.input.GenerateFrequentCustomersReportUseCase;
import com.axiserp.report.ports.input.GenerateInventoryReportUseCase;
import com.axiserp.report.ports.input.GenerateSalesReportUseCase;
import com.axiserp.report.ports.input.GenerateTopProductsReportUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController")
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock private GenerateSalesReportUseCase generateSalesReportUseCase;
    @Mock private GenerateInventoryReportUseCase generateInventoryReportUseCase;
    @Mock private GenerateTopProductsReportUseCase generateTopProductsReportUseCase;
    @Mock private GenerateDashboardUseCase generateDashboardUseCase;
    @Mock private GenerateFrequentCustomersReportUseCase generateFrequentCustomersReportUseCase;
    @Mock private PdfExportService pdfExportService;
    @Mock private ExcelExportService excelExportService;
    @Mock private CsvExportService csvExportService;
    @Mock private ReportAuditService reportAuditService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportController(
                        generateSalesReportUseCase, generateInventoryReportUseCase,
                        generateTopProductsReportUseCase, generateDashboardUseCase,
                        generateFrequentCustomersReportUseCase, pdfExportService,
                        excelExportService, csvExportService, reportAuditService))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/reports/sales - should return 200")
    void salesReport() throws Exception {
        var response = new SalesReportResponse(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                10, 10, java.math.BigDecimal.valueOf(1000), java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO, java.util.Map.of("CONFIRMADA", 5L),
                java.util.Map.of(), List.of());

        when(generateSalesReportUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSales").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/reports/inventory - should return 200")
    void inventoryReport() throws Exception {
        var response = new InventoryReportResponse(5, 1, 0, List.of());

        when(generateInventoryReportUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalProducts").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/reports/top-products - should return 200")
    void topProducts() throws Exception {
        var response = new TopProductsReportResponse(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of());

        when(generateTopProductsReportUseCase.execute(any(), any(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/top-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/reports/dashboard - should return 200")
    void dashboard() throws Exception {
        var response = new DashboardResponse(
                java.math.BigDecimal.valueOf(500), 5, 2, 1, 20, List.of());

        when(generateDashboardUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todaySalesCount").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/reports/sales/export/pdf - should return PDF")
    void exportPdf() throws Exception {
        byte[] pdfBytes = {1, 2, 3, 4, 5};
        when(pdfExportService.exportSalesReport(any(), any(), any())).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/v1/reports/sales/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("GET /api/v1/reports/sales/export/csv - should return CSV")
    void exportCsv() throws Exception {
        byte[] csvBytes = {1, 2, 3};
        when(csvExportService.exportSalesReport(any(), any(), any(), any(), any())).thenReturn(csvBytes);

        mockMvc.perform(get("/api/v1/reports/sales/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"));
    }

    @Test
    @DisplayName("GET /api/v1/reports/customers/frequent - should return 200")
    void frequentCustomers() throws Exception {
        var response = new FrequentCustomerReportResponse(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of());

        when(generateFrequentCustomersReportUseCase.execute(any(), any(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/reports/customers/frequent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/reports/audit - should return 200")
    void auditLog() throws Exception {
        var log = ExportLog.builder().id(UUID.randomUUID()).reportType("DAILY_SALES").build();
        when(reportAuditService.getAuditLog(any(), any(), any())).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/reports/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
