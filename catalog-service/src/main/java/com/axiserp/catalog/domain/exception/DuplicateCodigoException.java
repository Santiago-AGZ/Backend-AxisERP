package com.axiserp.catalog.domain.exception;

public class DuplicateCodigoException extends RuntimeException {
    public DuplicateCodigoException() {
        super("El codigo ya esta registrado en el sistema");
    }
}
