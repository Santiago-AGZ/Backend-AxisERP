package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.model.PurchaseStatus;

public interface UpdatePurchaseStatusUseCase {
    PurchaseResponse execute(UUID purchaseId, PurchaseStatus newStatus);
}
