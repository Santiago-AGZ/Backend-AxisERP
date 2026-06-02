package com.axiserp.catalog.domain.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("Producto no encontrado");
    }
}
