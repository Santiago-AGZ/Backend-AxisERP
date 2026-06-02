package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;

public interface InitializeInventoryUseCase {
    InventoryResponse initialize(InitializeInventoryRequest request, UUID createdBy);
}
