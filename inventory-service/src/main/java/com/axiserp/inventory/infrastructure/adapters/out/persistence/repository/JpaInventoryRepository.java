package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductId(UUID productId);
}
