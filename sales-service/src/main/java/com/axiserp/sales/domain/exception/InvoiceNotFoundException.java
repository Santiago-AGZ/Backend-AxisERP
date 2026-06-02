package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(UUID id) {
        super("Factura no encontrada con id: " + id);
    }

    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
