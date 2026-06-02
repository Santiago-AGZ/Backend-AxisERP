package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID id) {
        super("Cliente no encontrado con id: " + id);
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
