package com.axiserp.auth.domain.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("El token ha expirado");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}
