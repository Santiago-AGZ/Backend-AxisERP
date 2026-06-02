package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;

public interface JpaMovementRepository extends JpaRepository<InventoryMovementEntity, UUID> {
    List<InventoryMovementEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
