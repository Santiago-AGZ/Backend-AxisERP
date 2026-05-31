package com.axiserp.sales.ports.input;

import java.util.List;
import java.util.UUID;

import com.axiserp.sales.application.dto.response.SaleResponse;

public interface ListSalesUseCase {

    List<SaleResponse> list(UUID customerId, String status, int page, int size);
}
