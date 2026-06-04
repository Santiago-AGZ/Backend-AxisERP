package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;

public interface UpdateProductUseCase {
    ProductResponse update(UUID id, UpdateProductRequest request, UUID userId);
}
