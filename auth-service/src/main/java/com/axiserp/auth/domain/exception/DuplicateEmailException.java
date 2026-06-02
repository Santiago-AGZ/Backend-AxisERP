package com.axiserp.auth.domain.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("El correo ya está registrado en el sistema");
    }
}
