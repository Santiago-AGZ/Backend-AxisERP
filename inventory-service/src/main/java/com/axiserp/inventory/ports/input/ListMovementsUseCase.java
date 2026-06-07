package com.axiserp.inventory.ports.input;

import java.util.UUID;

import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.shared.PageResult;

public interface ListMovementsUseCase {
    PageResult<MovementResponse> listByProductId(UUID productId);
}
