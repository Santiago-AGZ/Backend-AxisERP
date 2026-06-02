package com.axiserp.purchase.ports.input;

import java.util.List;

import com.axiserp.purchase.application.dto.response.PurchaseResponse;

public interface ListPurchasesUseCase {
    List<PurchaseResponse> execute();
    List<PurchaseResponse> execute(String search, String status, int page, int size);
    long countAll();
    long countByFilters(String search, String status);
}
