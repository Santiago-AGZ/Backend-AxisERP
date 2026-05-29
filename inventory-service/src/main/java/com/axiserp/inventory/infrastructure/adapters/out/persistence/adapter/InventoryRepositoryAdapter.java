package com.axiserp.inventory.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private final JpaInventoryRepository jpaInventoryRepository;
    private final JpaMovementRepository jpaMovementRepository;

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return jpaInventoryRepository.findByProductId(productId).map(this::toDomain);
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);
        InventoryEntity saved = jpaInventoryRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public InventoryMovement saveMovement(InventoryMovement movement) {
        InventoryMovementEntity entity = toMovementEntity(movement);
        InventoryMovementEntity saved = jpaMovementRepository.save(entity);
        return toMovementDomain(saved);
    }

    @Override
    public Optional<InventoryMovement> findMovementById(UUID movementId) {
        return jpaMovementRepository.findById(movementId).map(this::toMovementDomain);
    }

    @Override
    public List<InventoryMovement> findMovementsByProductId(UUID productId) {
        return jpaMovementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toMovementDomain)
                .toList();
    }

    private Inventory toDomain(InventoryEntity entity) {
        return Inventory.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .currentStock(entity.getCurrentStock())
                .minStock(entity.getMinStock())
                .maxStock(entity.getMaxStock())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private InventoryEntity toEntity(Inventory domain) {
        return InventoryEntity.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .currentStock(domain.getCurrentStock())
                .minStock(domain.getMinStock())
                .maxStock(domain.getMaxStock())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private InventoryMovement toMovementDomain(InventoryMovementEntity entity) {
        return InventoryMovement.builder()
                .id(entity.getId())
                .inventoryId(entity.getInventoryId())
                .productId(entity.getProductId())
                .movementType(MovementType.valueOf(entity.getMovementType().name()))
                .quantity(entity.getQuantity())
                .previousStock(entity.getPreviousStock())
                .newStock(entity.getNewStock())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .justification(entity.getJustification())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private InventoryMovementEntity toMovementEntity(InventoryMovement domain) {
        return InventoryMovementEntity.builder()
                .id(domain.getId())
                .inventoryId(domain.getInventoryId())
                .productId(domain.getProductId())
                .movementType(domain.getMovementType())
                .quantity(domain.getQuantity())
                .previousStock(domain.getPreviousStock())
                .newStock(domain.getNewStock())
                .referenceType(domain.getReferenceType())
                .referenceId(domain.getReferenceId())
                .justification(domain.getJustification())
                .notes(domain.getNotes())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
