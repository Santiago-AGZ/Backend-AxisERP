package com.axiserp.inventory.domain.exception;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(UUID productId) {
        super("Inventario no encontrado para el producto: " + productId);
    }

    public InventoryNotFoundException(String message) {
        super(message);
    }
}
