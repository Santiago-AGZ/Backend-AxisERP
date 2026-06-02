package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.SaleResponse;

public interface VoidSaleUseCase {

    SaleResponse voidSale(UUID saleId);
}
