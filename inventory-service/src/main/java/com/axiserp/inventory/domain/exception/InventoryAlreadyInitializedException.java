package com.axiserp.inventory.domain.exception;

import java.util.UUID;

public class InventoryAlreadyInitializedException extends RuntimeException {

    public InventoryAlreadyInitializedException(UUID productId) {
        super("El inventario ya fue inicializado para el producto: " + productId);
    }
}
