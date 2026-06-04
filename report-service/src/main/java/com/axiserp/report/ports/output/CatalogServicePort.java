package com.axiserp.report.ports.output;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public interface CatalogServicePort {
    JsonNode fetchProducts(String search, UUID categoryId, boolean includeInactive, int page, int size);

    JsonNode fetchCategories(String search, boolean includeInactive, int page, int size);
}
