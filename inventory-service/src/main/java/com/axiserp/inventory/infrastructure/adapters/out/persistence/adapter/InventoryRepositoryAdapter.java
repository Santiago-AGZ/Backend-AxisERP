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

    @Override
    public long countMovementsByProductId(UUID productId) {
        return jpaMovementRepository.countByProductId(productId);
    }

    @Override
    public List<Inventory> findAll(int page, int size) {
        return jpaInventoryRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countAll() {
        return jpaInventoryRepository.count();
    }

    @Override
    public List<Inventory> findLowStock(int page, int size) {
        return jpaInventoryRepository.findLowStock(size, page * size)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countLowStock() {
        return jpaInventoryRepository.countLowStock();
    }

    @Override
    public List<Inventory> findByProductIds(List<UUID> productIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaInventoryRepository.findByProductIds(productIds, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByProductIds(List<UUID> productIds) {
        return jpaInventoryRepository.countByProductIds(productIds);
    }

    @Override
    public List<Inventory> findDepleted(int page, int size) {
        return jpaInventoryRepository.findDepleted(size, page * size)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countDepleted() {
        return jpaInventoryRepository.countDepleted();
    }

    private Inventory toDomain(InventoryEntity e) {
        Inventory inventory = Inventory.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .currentStock(e.getCurrentStock())
                .minStock(e.getMinStock())
                .maxStock(e.getMaxStock())
                .reservedStock(e.getReservedStock())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .lastMovementAt(e.getLastMovementAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
        inventory.setVersion(e.getVersion());
        return inventory;
    }

    private InventoryEntity toEntity(Inventory i) {
        InventoryEntity entity = InventoryEntity.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .currentStock(i.getCurrentStock())
                .minStock(i.getMinStock())
                .maxStock(i.getMaxStock() != null && i.getMaxStock() > 0 ? i.getMaxStock() : null)
                .reservedStock(i.getReservedStock())
                .createdBy(i.getCreatedBy())
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
