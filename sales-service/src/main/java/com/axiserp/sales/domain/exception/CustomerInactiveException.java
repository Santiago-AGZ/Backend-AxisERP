package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class CustomerInactiveException extends RuntimeException {

    public CustomerInactiveException(UUID id) {
        super("El cliente no esta activo: " + id);
    }
}
