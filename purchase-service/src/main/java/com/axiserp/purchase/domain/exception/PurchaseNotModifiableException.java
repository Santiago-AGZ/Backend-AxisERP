package com.axiserp.purchase.domain.exception;

import java.util.UUID;

public class PurchaseNotModifiableException extends RuntimeException {

    public PurchaseNotModifiableException(UUID id) {
        super("La compra no puede ser modificada en su estado actual: " + id);
    }

    public PurchaseNotModifiableException(String message) {
        super(message);
    }
}
