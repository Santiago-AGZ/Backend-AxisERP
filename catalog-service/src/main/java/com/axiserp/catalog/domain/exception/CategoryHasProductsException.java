package com.axiserp.catalog.domain.exception;

public class CategoryHasProductsException extends RuntimeException {
    public CategoryHasProductsException(int activeProductCount) {
        super("No se puede desactivar la categoria: tiene " + activeProductCount + " producto(s) activo(s) asociados. Desactive o reasigne los productos primero.");
    }
}
