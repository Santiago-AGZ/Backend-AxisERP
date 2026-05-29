package com.axiserp.purchase.ports.input;

import java.util.List;

import com.axiserp.purchase.application.dto.response.PurchaseResponse;

public interface ListPurchasesUseCase {
    List<PurchaseResponse> execute();
}
