package com.axiserp.purchase.domain.exception;

public class DuplicateNitException extends RuntimeException {

    public DuplicateNitException(String nit) {
        super("Ya existe un proveedor con el NIT: " + nit);
    }
}
