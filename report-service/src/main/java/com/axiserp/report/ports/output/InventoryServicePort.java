package com.axiserp.report.ports.output;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public interface InventoryServicePort {
    JsonNode fetchProducts(int page, int size, UUID categoryId);

    JsonNode fetchAlerts(int page, int size);

    JsonNode fetchProductById(UUID productId);
}
