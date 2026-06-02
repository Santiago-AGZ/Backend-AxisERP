package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId) {
        super("Stock insuficiente para el producto: " + productId);
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
