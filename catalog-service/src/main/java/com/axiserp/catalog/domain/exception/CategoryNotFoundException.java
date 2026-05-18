package com.axiserp.catalog.domain.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
        super("Categoria no encontrada");
    }
}
