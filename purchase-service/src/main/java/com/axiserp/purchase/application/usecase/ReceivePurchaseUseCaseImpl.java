package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.request.ReceiveItemRequest;
import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseItemResponse;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.input.ReceivePurchaseUseCase;
import com.axiserp.purchase.ports.output.InventoryServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceivePurchaseUseCaseImpl implements ReceivePurchaseUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReceivePurchaseUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;
    private final InventoryServicePort inventoryServicePort;

    @Override
    public PurchaseResponse execute(UUID purchaseId, ReceivePurchaseRequest request) {
        Purchase purchase = purchaseRepositoryPort.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        if (purchase.getStatus() != PurchaseStatus.PENDIENTE) {
            throw new PurchaseNotModifiableException(
                    "La compra debe estar en estado PENDIENTE para ser recibida: " + purchaseId);
        }

        Map<UUID, PurchaseItem> itemsById = purchase.getItems().stream()
                .collect(Collectors.toMap(PurchaseItem::getId, i -> i));

        for (ReceiveItemRequest receiveItem : request.getItems()) {
            PurchaseItem item = itemsById.get(receiveItem.getItemId());
            if (item == null) {
                throw new IllegalArgumentException(
                        "Item no encontrado en la compra: " + receiveItem.getItemId());
            }
            int newReceived = item.getReceivedQuantity() + receiveItem.getReceivedQuantity();
            if (newReceived > item.getQuantity()) {
                throw new IllegalArgumentException(
                        "La cantidad recibida supera la cantidad ordenada para el item: " + item.getId());
            }
            item.setReceivedQuantity(newReceived);

            // Register inventory entry for received quantity
            inventoryServicePort.registerEntry(
                    item.getProductId(),
                    receiveItem.getReceivedQuantity(),
                    "COMPRA",
                    purchaseId,
                    null);
            log.info("inventory_entry_registered productId={} qty={} purchaseId={}",
                    item.getProductId(), receiveItem.getReceivedQuantity(), purchaseId);
        }

        // Check if all items are fully received
        boolean allReceived = purchase.getItems().stream().allMatch(PurchaseItem::isFullyReceived);
        if (allReceived) {
            purchase.setStatus(PurchaseStatus.RECIBIDA);
        }

        purchase.setUpdatedAt(LocalDateTime.now());
        Purchase saved = purchaseRepositoryPort.save(purchase);
        log.info("purchase_received id={} status={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
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
