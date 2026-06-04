package com.axiserp.inventory.ports.output;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.axiserp.inventory.application.dto.response.ProductSummary;

public interface CatalogServicePort {
    List<UUID> findProductIdsByCategoryId(UUID categoryId);
    Map<UUID, ProductSummary> findProductSummaries(List<UUID> productIds);
}
