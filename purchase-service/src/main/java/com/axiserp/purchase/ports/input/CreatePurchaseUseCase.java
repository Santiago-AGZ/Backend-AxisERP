package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;

public interface CreatePurchaseUseCase {
    PurchaseResponse execute(CreatePurchaseRequest request, UUID createdBy);
}
