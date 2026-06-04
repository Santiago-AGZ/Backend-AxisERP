package com.axiserp.sales.ports.output;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.ProductSummary;

public interface CatalogServicePort {

    boolean productExists(UUID productId);

    ProductSummary findProductSummary(UUID productId);
}