package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.mapper.PurchaseMapper;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.input.CancelPurchaseUseCase;
import com.axiserp.purchase.ports.output.InventoryServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelPurchaseUseCaseImpl implements CancelPurchaseUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelPurchaseUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;
    private final InventoryServicePort inventoryServicePort;

    @Override
    public PurchaseResponse execute(UUID purchaseId) {
        Purchase purchase = purchaseRepositoryPort.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        if (!purchase.isModifiable()) {
            throw new PurchaseNotModifiableException(purchaseId);
        }

        // Reverse inventory movements if any items were received
        boolean hasReceivedItems = purchase.getItems().stream()
                .anyMatch(item -> item.getReceivedQuantity() > 0);
        if (hasReceivedItems) {
            inventoryServicePort.reverseMovements(purchaseId);
            log.info("inventory_movements_reversed purchaseId={}", purchaseId);
        }

        purchase.setStatus(PurchaseStatus.CANCELADA);
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepositoryPort.save(purchase);
        log.info("purchase_cancelled id={}", saved.getId());
        return PurchaseMapper.toResponse(saved);
    }
}
