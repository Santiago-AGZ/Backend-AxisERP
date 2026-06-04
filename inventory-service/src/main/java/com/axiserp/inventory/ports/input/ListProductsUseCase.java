package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.domain.model.PageResult;

public interface ListProductsUseCase {
    PageResult<ProductInventoryResponse> list(int page, int size, UUID categoryId);
    ProductInventoryResponse getByProductId(UUID productId);
}