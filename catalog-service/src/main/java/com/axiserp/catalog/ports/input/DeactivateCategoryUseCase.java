package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface DeactivateCategoryUseCase {
    CategoryResponse deactivate(UUID id, UUID updatedBy);
}
