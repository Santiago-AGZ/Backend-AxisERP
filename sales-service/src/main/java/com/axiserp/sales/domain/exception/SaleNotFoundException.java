package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class SaleNotFoundException extends RuntimeException {

    public SaleNotFoundException(UUID id) {
        super("Venta no encontrada con id: " + id);
    }
}
