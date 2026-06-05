package com.axiserp.report.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.report.application.dto.response.DashboardResponse;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateDashboardUseCaseImpl")
class GenerateDashboardUseCaseImplTest {

    @Mock
    private SalesServicePort salesServicePort;

    @Mock
    private InventoryServicePort inventoryServicePort;

    private GenerateDashboardUseCaseImpl useCase;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        useCase = new GenerateDashboardUseCaseImpl(salesServicePort, inventoryServicePort);
    }

    @Test
    @DisplayName("should aggregate dashboard data")
    void dashboard() {
        ArrayNode salesData = mapper.createArrayNode();
        ObjectNode sale1 = mapper.createObjectNode();
        sale1.put("id", UUID.randomUUID().toString());
        sale1.put("saleNumber", "V-000001");
        sale1.put("total", "100.00");
        sale1.put("status", "CONFIRMADA");
        sale1.put("createdAt", "2026-06-04T10:00:00");
        salesData.add(sale1);

        ObjectNode sale2 = mapper.createObjectNode();
        sale2.put("id", UUID.randomUUID().toString());
        sale2.put("saleNumber", "V-000002");
        sale2.put("total", "50.00");
        sale2.put("status", "PENDIENTE");
        sale2.put("createdAt", "2026-06-04T11:00:00");
        salesData.add(sale2);

        ObjectNode salesResponse = mapper.createObjectNode();
        salesResponse.set("data", salesData);
        ObjectNode salesPagination = mapper.createObjectNode();
        salesPagination.put("totalPages", 1);
        salesResponse.set("pagination", salesPagination);

        ArrayNode alertData = mapper.createArrayNode();
        alertData.add(mapper.createObjectNode().put("id", UUID.randomUUID().toString()));
        alertData.add(mapper.createObjectNode().put("id", UUID.randomUUID().toString()));
        ObjectNode alertResponse = mapper.createObjectNode();
        alertResponse.set("data", alertData);

        ObjectNode custResponse = mapper.createObjectNode();
        ObjectNode custPagination = mapper.createObjectNode();
        custPagination.put("totalRecords", 5);
        custResponse.set("pagination", custPagination);

        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(salesResponse);
        when(inventoryServicePort.fetchAlerts(anyInt(), anyInt())).thenReturn(alertResponse);
        when(salesServicePort.fetchCustomers(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(custResponse);

        DashboardResponse result = useCase.execute();

        assertEquals(0, new BigDecimal("150.00").compareTo(result.todayRevenue()));
        assertEquals(2, result.todaySalesCount());
        assertEquals(1, result.pendingSalesCount());
        assertEquals(2, result.lowStockCount());
        assertEquals(5, result.totalCustomers());
        assertEquals(2, result.recentSales().size());
    }

    @Test
    @DisplayName("should handle null responses gracefully")
    void nullResponses() {
        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(null);

        DashboardResponse result = useCase.execute();

        assertEquals(BigDecimal.ZERO.compareTo(result.todayRevenue()), 0);
        assertEquals(0, result.todaySalesCount());
        assertEquals(0, result.lowStockCount());
        assertEquals(0, result.totalCustomers());
    }
}
