package com.axiserp.catalog.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface GetCategoryUseCase {
    CategoryResponse getById(UUID id);
}
