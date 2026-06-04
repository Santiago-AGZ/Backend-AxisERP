package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface ReactivateCategoryUseCase {
    CategoryResponse reactivate(UUID id, UUID updatedBy);
}
