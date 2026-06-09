package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.purchase.application.dto.mapper.PurchaseMapper;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
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
    @Transactional
    public PurchaseResponse execute(UUID purchaseId, PurchaseStatus newStatus) {
        Purchase purchase = purchaseRepositoryPort.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        validateTransition(purchase, newStatus);

        purchase.setStatus(newStatus);
        purchase.setUpdatedAt(LocalDateTime.now());

        Purchase saved = purchaseRepositoryPort.save(purchase);
        log.info("purchase_status_updated id={} newStatus={}", saved.getId(), saved.getStatus());
        return PurchaseMapper.toResponse(saved);
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
}
