package com.axiserp.catalog.domain.exception;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException() {
        super("El precio de venta no puede ser menor al precio de compra");
    }
}
