package com.axiserp.auth.domain.service;

import com.axiserp.auth.domain.exception.WeakPasswordException;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final String UPPERCASE_PATTERN = "(?=.*[A-Z])";
    private static final String LOWERCASE_PATTERN = "(?=.*[a-z])";
    private static final String DIGIT_PATTERN = "(?=.*\\d)";
    private static final String SPECIAL_PATTERN = "(?=.*[@#$%^&*!])";
    private static final String NO_SPACES_PATTERN = "^\\S+$";

    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("La contraseña no puede estar vacía");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new WeakPasswordException(
                String.format("La contraseña debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH)
            );
        }

        if (!password.matches(UPPERCASE_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra mayúscula");
        }

        if (!password.matches(LOWERCASE_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra minúscula");
        }

        if (!password.matches(DIGIT_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos un número");
        }

        if (!password.matches(SPECIAL_PATTERN)) {
            throw new WeakPasswordException("La contraseña debe contener al menos un carácter especial (@#$%^&*!)");
        }

        if (!password.matches(NO_SPACES_PATTERN)) {
            throw new WeakPasswordException("La contraseña no puede contener espacios en blanco");
        }
    }
}
