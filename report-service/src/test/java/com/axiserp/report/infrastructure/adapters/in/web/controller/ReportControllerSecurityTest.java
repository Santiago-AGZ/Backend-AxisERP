package com.axiserp.report.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;
import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.domain.model.ExportLog;
import com.axiserp.report.ports.input.*;

@WebMvcTest(controllers = ReportController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ReportController Security")
class ReportControllerSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GenerateSalesReportUseCase generateSalesReportUseCase;
    @MockitoBean private GenerateInventoryReportUseCase generateInventoryReportUseCase;
    @MockitoBean private GenerateTopProductsReportUseCase generateTopProductsReportUseCase;
    @MockitoBean private GenerateDashboardUseCase generateDashboardUseCase;
    @MockitoBean private GenerateFrequentCustomersReportUseCase generateFrequentCustomersReportUseCase;
    @MockitoBean private ExportSalesPdfUseCase exportSalesPdfUseCase;
    @MockitoBean private ExportInventoryExcelUseCase exportInventoryExcelUseCase;
    @MockitoBean private ExportSalesCsvUseCase exportSalesCsvUseCase;
    @MockitoBean private GetExportAuditLogUseCase getExportAuditLogUseCase;


    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/sales ADMIN 200")
    void salesReport_asAdmin() throws Exception {
        when(generateSalesReportUseCase.execute(any(), any(), any(), any(), any()))
            .thenReturn(new SalesReportResponse(LocalDate.now(), LocalDate.now(), 10, 10, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, java.util.Map.of(), java.util.Map.of(), List.of()));
        mockMvc.perform(get("/api/v1/reports/sales")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/sales INVENTARIO 403")
    void salesReport_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/sales")).andExpect(status().isForbidden());
    }

    @Test @DisplayName("GET /api/v1/reports/sales UNAUTH 401")
    void salesReport_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/reports/sales")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/inventory ADMIN 200")
    void inventoryReport_asAdmin() throws Exception {
        when(generateInventoryReportUseCase.execute(any())).thenReturn(new InventoryReportResponse(5, 1, 0, List.of()));
        mockMvc.perform(get("/api/v1/reports/inventory")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/inventory INVENTARIO 200")
    void inventoryReport_asInventario() throws Exception {
        when(generateInventoryReportUseCase.execute(any())).thenReturn(new InventoryReportResponse(5, 1, 0, List.of()));
        mockMvc.perform(get("/api/v1/reports/inventory")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/reports/inventory VENDEDOR 403")
    void inventoryReport_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/reports/inventory")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/top-products ADMIN 200")
    void topProducts_asAdmin() throws Exception {
        when(generateTopProductsReportUseCase.execute(any(), any(), anyInt())).thenReturn(new TopProductsReportResponse(LocalDate.now(), LocalDate.now(), List.of()));
        mockMvc.perform(get("/api/v1/reports/top-products")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/top-products INVENTARIO 403")
    void topProducts_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/top-products")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/dashboard ADMIN 200")
    void dashboard_asAdmin() throws Exception {
        when(generateDashboardUseCase.execute()).thenReturn(new DashboardResponse(BigDecimal.TEN, 5, 2, 1, 20, List.of()));
        mockMvc.perform(get("/api/v1/reports/dashboard")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/dashboard INVENTARIO 403")
    void dashboard_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/dashboard")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/sales/export/pdf ADMIN 200")
    void exportPdf_asAdmin() throws Exception {
        when(exportSalesPdfUseCase.exportSalesPdf(any(), any(), any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/reports/sales/export/pdf")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/sales/export/pdf INVENTARIO 403")
    void exportPdf_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/sales/export/pdf")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/inventory/export/excel ADMIN 200")
    void exportExcel_asAdmin() throws Exception {
        when(exportInventoryExcelUseCase.exportInventoryExcel(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/reports/inventory/export/excel")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/inventory/export/excel INVENTARIO 200")
    void exportExcel_asInventario() throws Exception {
        when(exportInventoryExcelUseCase.exportInventoryExcel(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/reports/inventory/export/excel")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/reports/inventory/export/excel VENDEDOR 403")
    void exportExcel_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/reports/inventory/export/excel")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/customers/frequent ADMIN 200")
    void frequentCustomers_asAdmin() throws Exception {
        when(generateFrequentCustomersReportUseCase.execute(any(), any(), anyInt())).thenReturn(new FrequentCustomerReportResponse(LocalDate.now(), LocalDate.now(), List.of()));
        mockMvc.perform(get("/api/v1/reports/customers/frequent")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/customers/frequent INVENTARIO 403")
    void frequentCustomers_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/customers/frequent")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/sales/export/csv ADMIN 200")
    void exportCsv_asAdmin() throws Exception {
        when(exportSalesCsvUseCase.exportSalesCsv(any(), any(), any(), any(), any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/reports/sales/export/csv")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/sales/export/csv INVENTARIO 403")
    void exportCsv_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/sales/export/csv")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/reports/audit ADMIN 200")
    void auditLog_asAdmin() throws Exception {
        when(getExportAuditLogUseCase.getAuditLog(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/reports/audit")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/reports/audit INVENTARIO 403")
    void auditLog_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/reports/audit")).andExpect(status().isForbidden());
    }
}
