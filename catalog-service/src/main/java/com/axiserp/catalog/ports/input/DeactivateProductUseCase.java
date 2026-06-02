package com.axiserp.catalog.ports.input;

import java.util.UUID;

import com.axiserp.catalog.application.dto.response.ProductResponse;

public interface DeactivateProductUseCase {
    ProductResponse deactivate(UUID id);
}
