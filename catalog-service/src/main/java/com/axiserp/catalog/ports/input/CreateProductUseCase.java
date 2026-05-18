package com.axiserp.catalog.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.catalog.application.dto.request.CreateProductRequest;
import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;

public interface CreateProductUseCase {
    ProductResponse create(CreateProductRequest request, UUID createdBy);
}
