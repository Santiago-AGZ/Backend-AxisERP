package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class SaleNotModifiableException extends RuntimeException {

    public SaleNotModifiableException(UUID id) {
        super("La venta no puede modificarse en su estado actual: " + id);
    }

    public SaleNotModifiableException(String message) {
        super(message);
    }
}
