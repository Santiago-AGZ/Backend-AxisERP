package com.axiserp.inventory.ports.input;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.domain.model.PageResult;

public interface GetDepletedAlertsUseCase {
    PageResult<ProductInventoryResponse> execute(int page, int size);
}