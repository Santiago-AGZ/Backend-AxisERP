package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.SaleResponse;

public interface PaySaleUseCase {

    SaleResponse pay(UUID saleId);
}
