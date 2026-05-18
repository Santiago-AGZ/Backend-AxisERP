package com.axiserp.catalog.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.catalog.application.dto.response.ProductResponse;

public interface ListProductsUseCase {
    List<ProductResponse> list(String search, String codigo, UUID categoryId, boolean includeInactive, int page, int size);
}
