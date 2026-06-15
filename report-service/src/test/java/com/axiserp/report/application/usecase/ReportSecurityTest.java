package com.axiserp.report.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.infrastructure.adapters.in.web.controller.ReportController;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("[R9][R11] Report Security and Read-Only Tests")
class ReportSecurityTest {

    @Mock
    private SalesServicePort salesServicePort;

    @Mock
    private InventoryServicePort inventoryServicePort;

    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID saleId = UUID.randomUUID();

    @Test
    @DisplayName("[R9] Sales report should only read data, never write")
    void salesReport_onlyReadsData() {
        GenerateSalesReportUseCaseImpl useCase = new GenerateSalesReportUseCaseImpl(salesServicePort);

        ArrayNode data = mapper.createArrayNode();
        ObjectNode sale = mapper.createObjectNode();
        sale.put("id", saleId.toString());
        sale.put("saleNumber", "V-000001");
        sale.put("status", "CONFIRMADA");
        sale.put("total", "100.00");
        sale.put("tax", "19.00");
        sale.put("discount", "0.00");
        sale.put("createdBy", UUID.randomUUID().toString());
        sale.put("createdAt", "2026-06-04T10:00:00");
        data.add(sale);

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(salesServicePort.fetchSales(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        SalesReportResponse result = useCase.execute(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), null, null, null);

        assertNotNull(result);
        assertEquals(1, result.totalTransactions());
        verify(salesServicePort, times(1)).fetchSales(any(), any(), any(), any(), any(), anyInt(), anyInt());
        verifyNoMoreInteractions(salesServicePort);
    }

    @Test
    @DisplayName("[R9] Inventory report should only read data, never write")
    void inventoryReport_onlyReadsData() {
        GenerateInventoryReportUseCaseImpl useCase = new GenerateInventoryReportUseCaseImpl(inventoryServicePort);

        ArrayNode data = mapper.createArrayNode();
        ObjectNode item = mapper.createObjectNode();
        item.put("productId", UUID.randomUUID().toString());
        item.put("productName", "Test Product");
        item.put("currentStock", 50);
        item.put("minStock", 10);
        data.add(item);

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(inventoryServicePort.fetchProducts(anyInt(), anyInt(), isNull()))
                .thenReturn(response);

        InventoryReportResponse result = useCase.execute(null);

        assertNotNull(result);
        assertEquals(1, result.totalProducts());
        verify(inventoryServicePort, times(1)).fetchProducts(anyInt(), anyInt(), isNull());
        verifyNoMoreInteractions(inventoryServicePort);
    }

    @Test
    @DisplayName("[R9] Top products report should only read data")
    void topProductsReport_onlyReadsData() {
        GenerateTopProductsReportUseCaseImpl useCase = new GenerateTopProductsReportUseCaseImpl(salesServicePort);

        ArrayNode data = mapper.createArrayNode();
        ObjectNode sale = mapper.createObjectNode();
        sale.put("id", UUID.randomUUID().toString());
        sale.put("saleNumber", "V-000001");
        sale.put("status", "CONFIRMADA");
        sale.put("total", "100.00");
        sale.put("tax", "19.00");
        sale.put("discount", "0.00");

        ArrayNode items = mapper.createArrayNode();
        ObjectNode item = mapper.createObjectNode();
        item.put("productId", UUID.randomUUID().toString());
        item.put("productName", "Top Product");
        item.put("quantity", 10);
        item.put("subtotal", "100.00");
        items.add(item);
        sale.set("items", items);
        data.add(sale);

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(response);

        TopProductsReportResponse result = useCase.execute(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 10);

        assertNotNull(result);
        assertEquals(1, result.rankings().size());
        verify(salesServicePort, atLeastOnce()).fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt());
        verifyNoMoreInteractions(salesServicePort);
    }

    @Test
    @DisplayName("[R9] Dashboard should only read data, not write")
    void dashboard_onlyReadsData() {
        GenerateDashboardUseCaseImpl useCase = new GenerateDashboardUseCaseImpl(salesServicePort, inventoryServicePort);

        ArrayNode salesData = mapper.createArrayNode();
        ObjectNode sale = mapper.createObjectNode();
        sale.put("id", UUID.randomUUID().toString());
        sale.put("saleNumber", "V-000001");
        sale.put("total", "100.00");
        sale.put("status", "CONFIRMADA");
        sale.put("createdAt", "2026-06-04T10:00:00");
        salesData.add(sale);

        ObjectNode salesResponse = mapper.createObjectNode();
        salesResponse.set("data", salesData);
        ObjectNode sp = mapper.createObjectNode();
        sp.put("totalPages", 1);
        salesResponse.set("pagination", sp);

        ArrayNode alertData = mapper.createArrayNode();
        ObjectNode alert = mapper.createObjectNode();
        alert.put("id", UUID.randomUUID().toString());
        alertData.add(alert);
        ObjectNode alertResponse = mapper.createObjectNode();
        alertResponse.set("data", alertData);

        ObjectNode custResponse = mapper.createObjectNode();
        ObjectNode cp = mapper.createObjectNode();
        cp.put("totalRecords", 5);
        custResponse.set("pagination", cp);

        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(salesResponse);
        when(inventoryServicePort.fetchAlerts(anyInt(), anyInt())).thenReturn(alertResponse);
        when(salesServicePort.fetchCustomers(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(custResponse);

        DashboardResponse result = useCase.execute();

        assertNotNull(result);
        verify(salesServicePort, atLeastOnce()).fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt());
        verify(salesServicePort, atLeastOnce()).fetchCustomers(any(), anyBoolean(), anyInt(), anyInt());
        verify(inventoryServicePort, atLeastOnce()).fetchAlerts(anyInt(), anyInt());
        verifyNoMoreInteractions(salesServicePort);
        verifyNoMoreInteractions(inventoryServicePort);
    }

    @Test
    @DisplayName("[R11] Report endpoints require ADMIN or INVENTARIO role via @PreAuthorize")
    void reportEndpoints_havePreAuthorize() throws Exception {
        var controllerMethods = ReportController.class.getDeclaredMethods();
        for (var method : controllerMethods) {
            if (!method.getName().startsWith("get") && !method.getName().startsWith("export")) {
                continue;
            }
            var preAuthorize = method.getAnnotation(
                    org.springframework.security.access.prepost.PreAuthorize.class);
            assertNotNull(preAuthorize,
                    "Report endpoint " + method.getName() + " must have @PreAuthorize");
            assertTrue(preAuthorize.value().contains("ADMIN") || preAuthorize.value().contains("INVENTARIO"),
                    "@PreAuthorize on " + method.getName() + " must require ADMIN or INVENTARIO role");
        }
    }
}
