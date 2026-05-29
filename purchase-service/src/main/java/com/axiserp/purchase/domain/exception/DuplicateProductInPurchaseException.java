package com.axiserp.purchase.domain.exception;

import java.util.UUID;

public class DuplicateProductInPurchaseException extends RuntimeException {

    public DuplicateProductInPurchaseException(UUID productId) {
        super("El producto ya fue agregado a esta compra: " + productId);
    }
}
