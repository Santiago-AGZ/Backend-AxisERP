package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface CreateCategoryUseCase {
    CategoryResponse create(CreateCategoryRequest request, UUID createdBy);
}
