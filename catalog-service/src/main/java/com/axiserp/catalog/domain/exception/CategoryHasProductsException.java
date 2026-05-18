package com.axiserp.catalog.domain.exception;

public class CategoryHasProductsException extends RuntimeException {
    public CategoryHasProductsException(int productCount) {
        super("No se puede eliminar: tiene " + productCount + " productos asociados");
    }
}
