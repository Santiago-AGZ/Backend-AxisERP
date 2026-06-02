package com.axiserp.auth.domain.exception;

public class WeakPasswordException extends RuntimeException {
    private static final String DEFAULT_MESSAGE =
        "La contraseña no cumple con los requisitos de seguridad requeridos";

    public WeakPasswordException() {
        super(DEFAULT_MESSAGE);
    }

    public WeakPasswordException(String message) {
        super(message);
    }

    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
