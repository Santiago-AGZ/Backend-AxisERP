package com.axiserp.purchase.domain.exception;

import java.util.UUID;

public class SupplierInactiveException extends RuntimeException {

    public SupplierInactiveException(UUID id) {
        super("El proveedor no esta activo: " + id);
    }

    public SupplierInactiveException(String message) {
        super(message);
    }
}
