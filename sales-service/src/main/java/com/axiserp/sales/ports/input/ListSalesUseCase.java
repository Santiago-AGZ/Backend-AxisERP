package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;

public interface ListSalesUseCase {

    PaginatedResponse<SaleResponse> list(UUID customerId, String status, UUID productId, int page, int size);
}
