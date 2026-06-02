package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class DuplicateProductInSaleException extends RuntimeException {

    public DuplicateProductInSaleException(UUID productId) {
        super("El producto ya fue agregado a la venta: " + productId);
    }
}
