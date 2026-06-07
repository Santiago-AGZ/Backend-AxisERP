package com.axiserp.inventory.application.usecase;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.application.shared.PageResult;
import com.axiserp.inventory.ports.input.ListMovementsUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListMovementsUseCaseImpl implements ListMovementsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListMovementsUseCaseImpl.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<MovementResponse> listByProductId(UUID productId) {
        List<InventoryMovement> movements = inventoryRepositoryPort.findMovementsByProductId(productId);

        log.debug("inventory_list_movements productId={} count={}", productId, movements.size());

        List<MovementResponse> content = movements.stream().map(this::toResponse).toList();
        long total = inventoryRepositoryPort.countMovementsByProductId(productId);
        return PageResult.<MovementResponse>builder()
                .content(content)
                .total(total)
                .build();
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
