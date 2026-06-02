package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.response.MovementResponse;

public interface RegisterExitUseCase {
    MovementResponse registerExit(UUID productId, int quantity, String referenceType, UUID referenceId, String notes, UUID createdBy);
}
