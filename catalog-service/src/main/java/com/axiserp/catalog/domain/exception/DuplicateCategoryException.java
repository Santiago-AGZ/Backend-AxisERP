package com.axiserp.catalog.domain.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException() {
        super("Ya existe una categoria con ese nombre");
    }
}
