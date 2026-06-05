package com.axiserp.report.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateInventoryReportUseCaseImpl")
class GenerateInventoryReportUseCaseImplTest {

    @Mock
    private InventoryServicePort inventoryServicePort;

    private GenerateInventoryReportUseCaseImpl useCase;
    private final ObjectMapper mapper = new ObjectMapper();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateInventoryReportUseCaseImpl(inventoryServicePort);
    }

    private ObjectNode createProductNode(String id, String name, int stock, int minStock, boolean lowStock) {
        ObjectNode node = mapper.createObjectNode();
        node.put("productId", id);
        node.put("productName", name);
        node.put("currentStock", stock);
        node.put("minStock", minStock);
        node.put("lowStock", lowStock);
        return node;
    }

    @Test
    @DisplayName("should categorize products by stock status")
    void categorizeProducts() {
        ArrayNode data = mapper.createArrayNode();
        data.add(createProductNode(productId.toString(), "Product A", 10, 5, false));
        data.add(createProductNode(UUID.randomUUID().toString(), "Product B", 2, 5, true));
        data.add(createProductNode(UUID.randomUUID().toString(), "Product C", 0, 5, false));

        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 1);
        response.set("pagination", pagination);

        when(inventoryServicePort.fetchProducts(anyInt(), anyInt(), any())).thenReturn(response);

        InventoryReportResponse result = useCase.execute(null);

        assertEquals(3, result.totalProducts());
        assertEquals(2, result.lowStockCount());
        assertEquals(1, result.depletedCount());
        assertEquals(3, result.items().size());
    }

    @Test
    @DisplayName("should handle null response")
    void nullResponse() {
        when(inventoryServicePort.fetchProducts(anyInt(), anyInt(), any())).thenReturn(null);

        InventoryReportResponse result = useCase.execute(null);

        assertEquals(0, result.totalProducts());
        assertEquals(0, result.lowStockCount());
        assertTrue(result.items().isEmpty());
    }

    @Test
    @DisplayName("should handle empty data")
    void emptyData() {
        ArrayNode data = mapper.createArrayNode();
        ObjectNode response = mapper.createObjectNode();
        response.set("data", data);
        ObjectNode pagination = mapper.createObjectNode();
        pagination.put("totalPages", 0);
        response.set("pagination", pagination);

        when(inventoryServicePort.fetchProducts(anyInt(), anyInt(), any())).thenReturn(response);

        InventoryReportResponse result = useCase.execute(null);

        assertEquals(0, result.totalProducts());
        assertTrue(result.items().isEmpty());
    }
}
