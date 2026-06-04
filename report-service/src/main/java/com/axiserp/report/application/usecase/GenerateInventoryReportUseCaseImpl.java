package com.axiserp.report.application.usecase;

import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.application.dto.response.InventoryReportResponse.InventoryItem;
import com.axiserp.report.ports.input.GenerateInventoryReportUseCase;
import com.axiserp.report.ports.output.InventoryServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GenerateInventoryReportUseCaseImpl implements GenerateInventoryReportUseCase {

    private final InventoryServicePort inventoryServicePort;

    private static final int MAX_PAGES = 20;
    private static final int PAGE_SIZE = 50;

    @Override
    public InventoryReportResponse execute(UUID categoryId) {
        List<JsonNode> allProducts = fetchAllProducts(categoryId);

        long lowStockCount = 0;
        long depletedCount = 0;
        List<InventoryItem> items = new ArrayList<>();

        for (JsonNode prod : allProducts) {
            int currentStock = prod.get("currentStock").asInt(0);
            int minStock = prod.get("minStock").asInt(0);
            boolean lowStock = prod.has("lowStock") && prod.get("lowStock").asBoolean(false);
            boolean depleted = currentStock <= 0;

            if (lowStock || depleted) {
                lowStockCount++;
            }
            if (depleted) {
                depletedCount++;
            }

            String productName = prod.has("productName") && !prod.get("productName").isNull()
                    ? prod.get("productName").asText() : "";

            items.add(new InventoryItem(
                    UUID.fromString(prod.get("productId").asText()),
                    productName,
                    currentStock,
                    minStock,
                    lowStock,
                    depleted
            ));
        }

        return new InventoryReportResponse(
                allProducts.size(),
                lowStockCount,
                depletedCount,
                items
        );
    }

    private List<JsonNode> fetchAllProducts(UUID categoryId) {
        List<JsonNode> allProducts = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            JsonNode response = inventoryServicePort.fetchProducts(page + 1, PAGE_SIZE, categoryId);
            if (response == null) break;

            JsonNode data = response.get("data");
            if (data == null || !data.isArray() || data.isEmpty()) break;

            StreamSupport.stream(data.spliterator(), false).forEach(allProducts::add);

            JsonNode pagination = response.get("pagination");
            if (pagination == null) break;

            int totalPages = pagination.has("totalPages") ? pagination.get("totalPages").asInt() : 1;
            if (page >= totalPages - 1) break;
        }
        return allProducts;
    }
}
