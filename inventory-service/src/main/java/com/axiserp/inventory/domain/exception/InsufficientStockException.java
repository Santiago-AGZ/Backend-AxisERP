package com.axiserp.inventory.domain.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(int requested, int available) {
        super("Stock insuficiente: solicitado=" + requested + ", disponible=" + available);
    }
}
