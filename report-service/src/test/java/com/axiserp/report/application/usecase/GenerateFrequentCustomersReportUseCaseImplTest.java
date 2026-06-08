package com.axiserp.report.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.report.application.dto.response.FrequentCustomerReportResponse;
import com.axiserp.report.application.service.ReportAuditService;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateFrequentCustomersReportUseCaseImpl")
class GenerateFrequentCustomersReportUseCaseImplTest {

    @Mock
    private SalesServicePort salesServicePort;

    @Mock
    private ReportAuditService reportAuditService;

    private GenerateFrequentCustomersReportUseCaseImpl useCase;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateFrequentCustomersReportUseCaseImpl(salesServicePort, reportAuditService);
    }

    @Test
    @DisplayName("should aggregate customers by frequency")
    void aggregateCustomers() {
        ArrayNode salesData = mapper.createArrayNode();

        ObjectNode sale1 = mapper.createObjectNode();
        sale1.put("id", UUID.randomUUID().toString());
        sale1.put("total", "100.00");
        sale1.put("status", "CONFIRMADA");
        sale1.put("customerId", customerId.toString());
        sale1.put("createdAt", "2026-06-04T10:00:00");
        salesData.add(sale1);

        ObjectNode sale2 = mapper.createObjectNode();
        sale2.put("id", UUID.randomUUID().toString());
        sale2.put("total", "50.00");
        sale2.put("status", "CONFIRMADA");
        sale2.put("customerId", customerId.toString());
        sale2.put("createdAt", "2026-06-05T10:00:00");
        salesData.add(sale2);

        ObjectNode salesResponse = mapper.createObjectNode();
        salesResponse.set("data", salesData);
        ObjectNode salesPagination = mapper.createObjectNode();
        salesPagination.put("totalPages", 1);
        salesResponse.set("pagination", salesPagination);

        ArrayNode customerData = mapper.createArrayNode();
        ObjectNode custNode = mapper.createObjectNode();
        custNode.put("id", customerId.toString());
        custNode.put("name", "Test Client");
        customerData.add(custNode);

        ObjectNode customerResponse = mapper.createObjectNode();
        customerResponse.set("data", customerData);
        ObjectNode custPagination = mapper.createObjectNode();
        custPagination.put("totalPages", 1);
        customerResponse.set("pagination", custPagination);

        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(salesResponse);
        when(salesServicePort.fetchCustomers(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(customerResponse);

        FrequentCustomerReportResponse result = useCase.execute(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 5);

        assertEquals(1, result.customers().size());
        assertEquals("Test Client", result.customers().get(0).customerName());
        assertEquals(2, result.customers().get(0).totalVisits());
        assertEquals(0, new BigDecimal("150.00").compareTo(result.customers().get(0).totalSpent()));

        verify(reportAuditService).logReportGeneration(eq("CUSTOMER_FREQUENCY"), any(), any(), any());
    }

    @Test
    @DisplayName("should handle empty sales data")
    void emptySales() {
        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(null);

        FrequentCustomerReportResponse result = useCase.execute(null, null, 5);

        assertTrue(result.customers().isEmpty());
    }
}
