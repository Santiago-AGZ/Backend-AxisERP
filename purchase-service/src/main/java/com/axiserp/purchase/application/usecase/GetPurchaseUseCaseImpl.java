package com.axiserp.purchase.application.usecase;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.response.PurchaseItemResponse;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.ports.input.GetPurchaseUseCase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPurchaseUseCaseImpl implements GetPurchaseUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPurchaseUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;

    @Override
    public PurchaseResponse execute(UUID id) {
        Purchase purchase = purchaseRepositoryPort.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException(id));
        log.info("purchase_get id={}", id);
        return toResponse(purchase);
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        List<PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplierId())
                .purchaseNumber(purchase.getPurchaseNumber())
                .status(purchase.getStatus())
                .items(itemResponses)
                .subtotal(purchase.getSubtotal())
                .tax(purchase.getTax())
                .total(purchase.getTotal())
                .notes(purchase.getNotes())
                .createdBy(purchase.getCreatedBy())
                .updatedBy(purchase.getUpdatedBy())
                .createdAt(purchase.getCreatedAt())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }

    private PurchaseItemResponse toItemResponse(PurchaseItem item) {
        return PurchaseItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .pendingQuantity(item.pendingQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
