package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.response.PurchaseItemResponse;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.input.UpdatePurchaseStatusUseCase;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePurchaseStatusUseCaseImpl implements UpdatePurchaseStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdatePurchaseStatusUseCaseImpl.class);

    private final PurchaseRepositoryPort purchaseRepositoryPort;

    @Override
    public PurchaseResponse execute(UUID purchaseId, PurchaseStatus newStatus) {
        Purchase purchase = purchaseRepositoryPort.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        validateTransition(purchase, newStatus);

        purchase.setStatus(newStatus);
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepositoryPort.save(purchase);
        log.info("purchase_status_updated id={} newStatus={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    private void validateTransition(Purchase purchase, PurchaseStatus newStatus) {
        PurchaseStatus current = purchase.getStatus();

        if (newStatus == PurchaseStatus.CANCELADA) {
            if (!purchase.isModifiable()) {
                throw new PurchaseNotModifiableException(purchase.getId());
            }
            return;
        }

        boolean valid = switch (current) {
            case BORRADOR -> newStatus == PurchaseStatus.PENDIENTE;
            case RECIBIDA -> newStatus == PurchaseStatus.PAGADA;
            default -> false;
        };

        if (!valid) {
            throw new PurchaseNotModifiableException(
                    "Transicion de estado no permitida: " + current + " -> " + newStatus);
        }
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
