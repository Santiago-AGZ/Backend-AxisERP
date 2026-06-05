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

import com.axiserp.report.application.dto.response.TopProductsReportResponse;
import com.axiserp.report.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateTopProductsReportUseCaseImpl")
class GenerateTopProductsReportUseCaseImplTest {

    @Mock
    private SalesServicePort salesServicePort;

    private GenerateTopProductsReportUseCaseImpl useCase;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID productA = UUID.randomUUID();
    private final UUID productB = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateTopProductsReportUseCaseImpl(salesServicePort);
    }

    @Test
    @DisplayName("should aggregate product sales from all sales")
    void aggregateProducts() {
        ArrayNode data = mapper.createArrayNode();

        ObjectNode sale1 = mapper.createObjectNode();
        sale1.put("id", UUID.randomUUID().toString());
        sale1.put("total", "100.00");
        sale1.put("status", "CONFIRMADA");
        ArrayNode items1 = mapper.createArrayNode();
        ObjectNode item1 = mapper.createObjectNode();
        item1.put("productId", productA.toString());
        item1.put("productName", "Product A");
        item1.put("quantity", 3);
        item1.put("subtotal", "60.00");
        items1.add(item1);
        ObjectNode item2 = mapper.createObjectNode();
        item2.put("productId", productB.toString());
        item2.put("productName", "Product B");
        item2.put("quantity", 1);
        item2.put("subtotal", "40.00");
        items1.add(item2);
        sale1.set("items", items1);

        ObjectNode sale2 = mapper.createObjectNode();
        sale2.put("id", UUID.randomUUID().toString());
        sale2.put("total", "30.00");
        sale2.put("status", "CONFIRMADA");
        ArrayNode items2 = mapper.createArrayNode();
        ObjectNode item3 = mapper.createObjectNode();
        item3.put("productId", productA.toString());
        item3.put("productName", "Product A");
        item3.put("quantity", 2);
        item3.put("subtotal", "30.00");
        items2.add(item3);
        sale2.set("items", items2);

        data.add(sale1);
        data.add(sale2);

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(response);

        TopProductsReportResponse result = useCase.execute(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 5);

        assertEquals(2, result.rankings().size());
        assertEquals("Product A", result.rankings().get(0).productName());
        assertEquals(5, result.rankings().get(0).totalQuantity());
        assertEquals(1, result.rankings().get(0).position());
        assertEquals("Product B", result.rankings().get(1).productName());
        assertEquals(1, result.rankings().get(1).totalQuantity());
        assertEquals(2, result.rankings().get(1).position());
    }

    @Test
    @DisplayName("should handle empty sales data")
    void emptySales() {
        when(salesServicePort.fetchSales(any(), any(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(null);

        TopProductsReportResponse result = useCase.execute(null, null, 5);

        assertTrue(result.rankings().isEmpty());
    }
}
