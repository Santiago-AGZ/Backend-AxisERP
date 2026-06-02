package com.axiserp.inventory.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.inventory.application.dto.response.MovementResponse;

public interface ListMovementsUseCase {
    List<MovementResponse> listByProductId(UUID productId);
}
