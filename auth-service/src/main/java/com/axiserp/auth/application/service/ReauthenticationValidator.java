package com.axiserp.auth.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReauthenticationValidator {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public void validate(UUID userId, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar su contraseña actual para confirmar la operación");
        }

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("La contraseña local no está configurada. Use Supabase Auth para gestionar este usuario.");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña proporcionada no es correcta");
        }
    }
}
