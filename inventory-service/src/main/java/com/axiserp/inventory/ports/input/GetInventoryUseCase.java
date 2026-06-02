package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.response.InventoryResponse;

public interface GetInventoryUseCase {
    InventoryResponse getByProductId(UUID productId);
}
