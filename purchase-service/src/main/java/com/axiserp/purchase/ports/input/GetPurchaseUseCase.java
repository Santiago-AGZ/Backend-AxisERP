package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.response.PurchaseResponse;

public interface GetPurchaseUseCase {
    PurchaseResponse execute(UUID id);
}
