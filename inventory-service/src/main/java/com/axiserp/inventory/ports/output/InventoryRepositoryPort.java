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
}
