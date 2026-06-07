package com.axiserp.inventory.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;

public interface InventoryRepositoryPort {
    Optional<Inventory> findByProductId(UUID productId);
    Inventory save(Inventory inventory);
    InventoryMovement saveMovement(InventoryMovement movement);
    Optional<InventoryMovement> findMovementById(UUID movementId);
    List<InventoryMovement> findMovementsByProductId(UUID productId);
    long countMovementsByProductId(UUID productId);
    List<Inventory> findAll(int page, int size);
    long countAll();
    List<Inventory> findLowStock(int page, int size);
    long countLowStock();
    List<Inventory> findByProductIds(List<UUID> productIds, int page, int size);
    long countByProductIds(List<UUID> productIds);
    List<Inventory> findDepleted(int page, int size);
    long countDepleted();
}
