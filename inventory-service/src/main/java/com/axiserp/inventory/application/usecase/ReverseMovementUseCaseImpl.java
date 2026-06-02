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
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.input.ReverseMovementUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReverseMovementUseCaseImpl implements ReverseMovementUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReverseMovementUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final AuditService auditService;

    @Override
    @Transactional
    public MovementResponse reverse(UUID movementId, String justification, UUID createdBy) {
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("La justificacion es obligatoria para anulaciones");
        }

        InventoryMovement original = inventoryRepositoryPort.findMovementById(movementId)
                .orElseThrow(() -> new InventoryNotFoundException("Movimiento no encontrado: " + movementId));

        Inventory inventory = inventoryRepositoryPort.findByProductId(original.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(original.getProductId()));

        int previousStock = inventory.getCurrentStock();
        MovementType originalType = original.getMovementType();

        // Reverse: undo the effect of the original movement
        if (originalType == MovementType.ENTRADA
                || originalType == MovementType.AJUSTE_POSITIVO
                || originalType == MovementType.DEVOLUCION
                || originalType == MovementType.INVENTARIO_INICIAL) {
            // Original added stock → reversal removes it
            if (!inventory.canExit(original.getQuantity())) {
                throw new InsufficientStockException(original.getQuantity(), inventory.getCurrentStock());
            }
            inventory.setCurrentStock(previousStock - original.getQuantity());
        } else {
            // Original removed stock (SALIDA, AJUSTE_NEGATIVO) → reversal adds it back
            inventory.setCurrentStock(previousStock + original.getQuantity());
        }

        Inventory saved = inventoryRepositoryPort.save(inventory);

        InventoryMovement reversal = InventoryMovement.builder()
                .inventoryId(saved.getId())
                .productId(saved.getProductId())
                .movementType(MovementType.ANULACION)
                .quantity(original.getQuantity())
                .previousStock(previousStock)
                .newStock(saved.getCurrentStock())
                .referenceType("MOVIMIENTO")
                .referenceId(movementId)
                .justification(justification)
                .createdBy(createdBy)
                .build();

        InventoryMovement savedReversal = inventoryRepositoryPort.saveMovement(reversal);

        auditService.logReversal(saved.getProductId(), createdBy, movementId, original.getQuantity());

        log.info("inventory_reversal movementId={} productId={} originalType={} previousStock={} newStock={}", movementId, saved.getProductId(), originalType, previousStock, saved.getCurrentStock());

        return toResponse(savedReversal);
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
