package com.axiserp.inventory.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InsufficientStockException;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.exception.NegativeQuantityException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.input.RegisterExitUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterExitUseCaseImpl implements RegisterExitUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterExitUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public MovementResponse registerExit(UUID productId, int quantity, String referenceType, UUID referenceId, String notes, UUID createdBy) {
        if (quantity <= 0) {
            throw new NegativeQuantityException();
        }

        Inventory inventory = inventoryRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        if (!inventory.canExit(quantity)) {
            throw new InsufficientStockException(quantity, inventory.getCurrentStock());
        }

        int previousStock = inventory.getCurrentStock();
        inventory.setCurrentStock(previousStock - quantity);

        Inventory saved = inventoryRepositoryPort.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .inventoryId(saved.getId())
                .productId(saved.getProductId())
                .movementType(MovementType.SALIDA)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(saved.getCurrentStock())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .notes(notes)
                .createdBy(createdBy)
                .build();

        InventoryMovement savedMovement = inventoryRepositoryPort.saveMovement(movement);

        auditService.logStockExit(productId, createdBy, quantity, previousStock, saved.getCurrentStock());

        log.info("inventory_exit productId={} quantity={} previousStock={} newStock={}", productId, quantity, previousStock, saved.getCurrentStock());

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
