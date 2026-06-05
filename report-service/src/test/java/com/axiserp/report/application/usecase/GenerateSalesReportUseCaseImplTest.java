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

import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateSalesReportUseCaseImpl")
class GenerateSalesReportUseCaseImplTest {

    @Mock
    private SalesServicePort salesServicePort;

    private GenerateSalesReportUseCaseImpl useCase;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID saleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateSalesReportUseCaseImpl(salesServicePort);
    }

    private ObjectNode createSaleNode(String id, String status, String total, String createdAt) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", id);
        node.put("saleNumber", "V-000001");
        node.put("status", status);
        node.put("total", total);
        node.put("tax", "0.00");
        node.put("discount", "0.00");
        node.put("createdBy", userId.toString());
        if (createdAt != null) node.put("createdAt", createdAt);
        return node;
    }

    @Test
    @DisplayName("should aggregate sales from single page")
    void singlePage() {
        ArrayNode data = mapper.createArrayNode();
        data.add(createSaleNode(saleId.toString(), "CONFIRMADA", "100.00", "2026-06-04T10:00:00"));
        data.add(createSaleNode(UUID.randomUUID().toString(), "PENDIENTE", "50.00", "2026-06-04T11:00:00"));

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(salesServicePort.fetchSales(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        SalesReportResponse result = useCase.execute(LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 4), null, null, null);

        assertEquals(2, result.totalTransactions());
        assertEquals(0, new BigDecimal("150.00").compareTo(result.totalRevenue()));
        assertEquals(2, result.salesByStatus().size());
        assertEquals(1L, result.salesByStatus().get("CONFIRMADA"));
        assertEquals(1L, result.salesByStatus().get("PENDIENTE"));
        assertEquals(2, result.recentSales().size());
    }

    @Test
    @DisplayName("should handle empty response")
    void emptyResponse() {
        when(salesServicePort.fetchSales(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(null);

        SalesReportResponse result = useCase.execute(null, null, null, null, null);

        assertEquals(0, result.totalTransactions());
        assertEquals(BigDecimal.ZERO.compareTo(result.totalRevenue()), 0);
        assertTrue(result.salesByStatus().isEmpty());
        assertTrue(result.recentSales().isEmpty());
    }

    @Test
    @DisplayName("should handle multiple pages")
    void multiplePages() {
        ArrayNode page1Data = mapper.createArrayNode();
        page1Data.add(createSaleNode(saleId.toString(), "CONFIRMADA", "50.00", "2026-06-04T10:00:00"));
        ObjectNode page1 = mapper.createObjectNode();
        page1.set("data", page1Data);
        ObjectNode pag1 = mapper.createObjectNode();
        pag1.put("totalPages", 2);
        page1.set("pagination", pag1);

        ArrayNode page2Data = mapper.createArrayNode();
        page2Data.add(createSaleNode(UUID.randomUUID().toString(), "CONFIRMADA", "75.00", "2026-06-04T11:00:00"));
        ObjectNode page2 = mapper.createObjectNode();
        page2.set("data", page2Data);
        ObjectNode pag2 = mapper.createObjectNode();
        pag2.put("totalPages", 2);
        page2.set("pagination", pag2);

        when(salesServicePort.fetchSales(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page1, page2);

        SalesReportResponse result = useCase.execute(LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 4), null, null, null);

        assertEquals(2, result.totalTransactions());
        assertEquals(0, new BigDecimal("125.00").compareTo(result.totalRevenue()));
    }
}
