package com.axiserp.auth.domain.exception;

public class UserInactiveException extends RuntimeException {

    public UserInactiveException() {
        super("Usuario inactivo. Contacte al administrador");
    }
}
