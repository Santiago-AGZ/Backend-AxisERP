package com.axiserp.inventory.domain.exception;

import java.util.UUID;

public class ProductNotActiveException extends RuntimeException {

    public ProductNotActiveException(UUID productId, String status) {
        super("El producto " + productId + " no puede inicializar inventario porque su estado es: " + status);
    }
}
