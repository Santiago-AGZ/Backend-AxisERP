package com.axiserp.inventory.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaInventoryRepository;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaMovementRepository;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private InventoryEntity toEntity(Inventory i) {
        InventoryEntity entity = InventoryEntity.builder().createdBy(i.getCreatedBy())
                .updatedBy(i.getUpdatedBy())
                .lastMovementAt(i.getLastMovementAt())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
        entity.setVersion(i.getVersion());
        return entity;
    }

    private InventoryMovement toMovementDomain(InventoryMovementEntity e) {
        return InventoryMovement.builder()
                .id(e.getId())
                .inventoryId(e.getInventoryId())
                .productId(e.getProductId())
                .movementType(MovementType.valueOf(e.getMovementType().name()))
                .quantity(e.getQuantity())
                .previousStock(e.getPreviousStock())
                .newStock(e.getNewStock())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                .justification(e.getJustification())
                .notes(e.getNotes())
                .createdBy(e.getUserId())
                .createdAt(e.getCreatedAt())
                .build();
    }

private InventoryMovementEntity toMovementEntity(InventoryMovement m) {
        return InventoryMovementEntity.builder()
                .id(m.getId())
                .inventoryId(m.getInventoryId())
                .productId(m.getProductId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .previousStock(m.getPreviousStock())
                .newStock(m.getNewStock())
                .referenceType(m.getReferenceType())
                .referenceId(m.getReferenceId())
                .justification(m.getJustification())
                .notes(m.getNotes())
                .userId(m.getCreatedBy())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
