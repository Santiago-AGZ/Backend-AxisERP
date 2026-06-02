package com.axiserp.sales.domain.exception;

public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String documentNumber) {
        super("Ya existe un cliente con el numero de documento: " + documentNumber);
    }
}
