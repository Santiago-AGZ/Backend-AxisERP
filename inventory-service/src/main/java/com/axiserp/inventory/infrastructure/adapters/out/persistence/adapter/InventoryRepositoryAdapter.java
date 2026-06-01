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
        InventoryEntity saved = jpaInventoryRepository.save(toEntity(inventory));
        return toDomain(saved);
    }

    @Override
    public InventoryMovement saveMovement(InventoryMovement movement) {
        InventoryMovementEntity saved = jpaMovementRepository.save(toMovementEntity(movement));
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

    private Inventory toDomain(InventoryEntity e) {
        return Inventory.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .currentStock(e.getCurrentStock())
                .minStock(e.getMinStock())
                .maxStock(e.getMaxStock())
                .reservedStock(e.getReservedStock())
                .version(e.getVersion())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .lastMovementAt(e.getLastMovementAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private InventoryEntity toEntity(Inventory i) {
        return InventoryEntity.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .currentStock(i.getCurrentStock())
                .minStock(i.getMinStock())
                .maxStock(i.getMaxStock() != null && i.getMaxStock() > 0 ? i.getMaxStock() : null)
                .reservedStock(i.getReservedStock())
                .version(i.getVersion())
                .createdBy(i.getCreatedBy())
                .updatedBy(i.getUpdatedBy())
                .lastMovementAt(i.getLastMovementAt())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private InventoryMovement toMovementDomain(InventoryMovementEntity e) {
        return InventoryMovement.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .movementType(MovementType.valueOf(e.getMovementType().name()))
                .quantity(e.getQuantity())
                .previousStock(e.getPreviousStock())
                .newStock(e.getNewStock())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                // BD tiene 'notes' para justificación/notas; 'user_id' para el autor
                .justification(e.getNotes())
                .notes(e.getNotes())
                .createdBy(e.getUserId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private InventoryMovementEntity toMovementEntity(InventoryMovement m) {
        // Priorizar justification sobre notes para el campo 'notes' de la BD
        String notesValue = m.getJustification() != null ? m.getJustification() : m.getNotes();
        return InventoryMovementEntity.builder()
                .id(m.getId())
                .productId(m.getProductId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .previousStock(m.getPreviousStock())
                .newStock(m.getNewStock())
                .referenceType(m.getReferenceType())
                .referenceId(m.getReferenceId())
                .notes(notesValue)
                .userId(m.getCreatedBy())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
