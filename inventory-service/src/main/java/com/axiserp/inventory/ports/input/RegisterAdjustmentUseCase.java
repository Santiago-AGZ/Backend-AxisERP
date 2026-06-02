package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.response.MovementResponse;

public interface RegisterAdjustmentUseCase {
    MovementResponse registerAdjustment(UUID productId, AdjustmentRequest request, UUID createdBy);
}
