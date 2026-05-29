package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;

public interface ReceivePurchaseUseCase {
    PurchaseResponse execute(UUID purchaseId, ReceivePurchaseRequest request);
}
