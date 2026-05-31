package com.axiserp.inventory.domain.exception;

public class NegativeQuantityException extends RuntimeException {

    public NegativeQuantityException() {
        super("La cantidad debe ser mayor que cero");
    }
}
