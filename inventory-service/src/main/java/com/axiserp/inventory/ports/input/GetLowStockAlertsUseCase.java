package com.axiserp.inventory.ports.input;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.application.shared.PageResult;

public interface GetLowStockAlertsUseCase {
    PageResult<ProductInventoryResponse> execute(int page, int size);
}
