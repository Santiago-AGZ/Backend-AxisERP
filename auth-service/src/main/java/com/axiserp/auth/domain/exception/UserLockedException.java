package com.axiserp.auth.domain.exception;

public class UserLockedException extends RuntimeException {

    public UserLockedException() {
        super("Cuenta bloqueada por demasiados intentos fallidos. Contacte al administrador");
    }

    public UserLockedException(String message) {
        super(message);
    }
}
