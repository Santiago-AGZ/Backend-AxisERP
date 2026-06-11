package com.axiserp.sales.domain.exception;

import java.util.UUID;

public class SaleAccessDeniedException extends RuntimeException {

    public SaleAccessDeniedException(UUID saleId) {
        super("No tiene permisos para acceder a la venta: " + saleId);
    }

    public SaleAccessDeniedException(String message) {
        super(message);
    }
}
