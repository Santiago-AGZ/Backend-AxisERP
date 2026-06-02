package com.axiserp.purchase.domain.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(UUID id) {
        super("Proveedor no encontrado con id: " + id);
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}
