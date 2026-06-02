package com.axiserp.purchase.domain.exception;

import java.util.UUID;

public class PurchaseNotFoundException extends RuntimeException {

    public PurchaseNotFoundException(UUID id) {
        super("Compra no encontrada con id: " + id);
    }

    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
