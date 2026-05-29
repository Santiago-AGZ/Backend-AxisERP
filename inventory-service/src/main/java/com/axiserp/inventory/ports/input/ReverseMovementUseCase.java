package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.response.MovementResponse;

public interface ReverseMovementUseCase {
    MovementResponse reverse(UUID movementId, String justification, UUID createdBy);
}
