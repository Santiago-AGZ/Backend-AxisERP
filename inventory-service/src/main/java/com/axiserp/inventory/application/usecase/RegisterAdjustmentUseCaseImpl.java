package com.axiserp.inventory.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InsufficientStockException;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.exception.NegativeQuantityException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.input.RegisterAdjustmentUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterAdjustmentUseCaseImpl implements RegisterAdjustmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterAdjustmentUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public MovementResponse registerAdjustment(UUID productId, AdjustmentRequest request, UUID createdBy) {
        if (request.getJustification() == null || request.getJustification().isBlank()) {
            throw new IllegalArgumentException("La justificacion es obligatoria para ajustes");
        }

        if (request.getQuantity() <= 0) {
            throw new NegativeQuantityException();
        }

        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        int previousStock = inventory.getCurrentStock();
        MovementType movementType;

        if (request.getAdjustmentType() == AdjustmentRequest.AdjustmentType.POSITIVO) {
            inventory.addStock(request.getQuantity());
            movementType = MovementType.AJUSTE_POSITIVO;
        } else {
            if (!inventory.canExit(request.getQuantity())) {
                throw new InsufficientStockException(request.getQuantity(), inventory.getCurrentStock());
            }
            inventory.subtractStock(request.getQuantity());
            movementType = MovementType.AJUSTE_NEGATIVO;
        }

        Inventory saved = inventoryRepositoryPort.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .inventoryId(saved.getId())
                .productId(saved.getProductId())
                .movementType(movementType)
                .quantity(request.getQuantity())
                .previousStock(previousStock)
                .newStock(saved.getCurrentStock())
                .justification(request.getJustification())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        InventoryMovement savedMovement = inventoryRepositoryPort.saveMovement(movement);

        auditService.logStockEntry(productId, createdBy, request.getQuantity(), previousStock, saved.getCurrentStock());

        log.info("inventory_adjustment productId={} type={} quantity={} previousStock={} newStock={}", productId, movementType, request.getQuantity(), previousStock, saved.getCurrentStock());

        return toResponse(savedMovement);
    }

    private MovementResponse toResponse(InventoryMovement movement) {
        return MovementResponse.builder()
                .id(movement.getId())
                .inventoryId(movement.getInventoryId())
                .productId(movement.getProductId())
                .movementType(movement.getMovementType().name())
                .quantity(movement.getQuantity())
                .previousStock(movement.getPreviousStock())
                .newStock(movement.getNewStock())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .justification(movement.getJustification())
                .notes(movement.getNotes())
                .createdBy(movement.getCreatedBy())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
