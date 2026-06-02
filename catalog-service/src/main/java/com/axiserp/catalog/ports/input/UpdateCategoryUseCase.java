package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface UpdateCategoryUseCase {
    CategoryResponse update(UUID id, UpdateCategoryRequest request, UUID updatedBy);
}
