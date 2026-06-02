package com.axiserp.auth.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
