package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.response.SaleResponse;

public interface CreateSaleUseCase {

    SaleResponse create(CreateSaleRequest request, UUID createdBy, boolean isAdmin);
}
