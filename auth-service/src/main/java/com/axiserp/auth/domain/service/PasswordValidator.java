package com.axiserp.auth.domain.service;

import com.axiserp.auth.domain.exception.WeakPasswordException;
import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("(?=.*[A-Z])");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("(?=.*[a-z])");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(?=.*\\d)");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("(?=.*[@#$%^&*!])");
    private static final Pattern NO_SPACES_PATTERN = Pattern.compile("^\\S+$");

    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("La contraseña no puede estar vacía");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new WeakPasswordException(
                String.format("La contraseña debe tener entre %d y %d caracteres", MIN_LENGTH, MAX_LENGTH)
            );
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra mayúscula");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("La contraseña debe contener al menos una letra minúscula");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("La contraseña debe contener al menos un número");
        }

        if (!SPECIAL_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("La contraseña debe contener al menos un carácter especial (@#$%^&*!)");
        }

        if (!NO_SPACES_PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException("La contraseña no puede contener espacios en blanco");
        }
    }
}
