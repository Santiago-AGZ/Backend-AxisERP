package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("[R46] ReauthenticationValidator Tests")
class ReauthenticationValidatorTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ReauthenticationValidator validator;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        validator = new ReauthenticationValidator(userRepositoryPort, passwordEncoder);
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$encodedHash")
                .build();
    }

    @Test
    @DisplayName("[R46] Should validate correct password")
    void validate_correctPassword_success() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", user.getPasswordHash())).thenReturn(true);

        assertDoesNotThrow(() -> validator.validate(userId, "correctPassword"));
    }

    @Test
    @DisplayName("[R46] Should throw IllegalArgumentException when password is null")
    void validate_nullPassword_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(userId, null));
        assertTrue(ex.getMessage().contains("contrase"));
    }

    @Test
    @DisplayName("[R46] Should throw IllegalArgumentException when password is blank")
    void validate_blankPassword_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(userId, "   "));
        assertTrue(ex.getMessage().contains("contrase"));
    }

    @Test
    @DisplayName("[R46] Should throw UserNotFoundException when user does not exist")
    void validate_userNotFound_throws() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> validator.validate(userId, "password"));
    }

    @Test
    @DisplayName("[R46] Should throw IllegalArgumentException when password hash is null")
    void validate_noLocalPassword_throws() {
        user.setPasswordHash(null);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(userId, "password"));
        assertTrue(ex.getMessage().contains("Supabase"));
    }

    @Test
    @DisplayName("[R46] Should throw IllegalArgumentException when password does not match")
    void validate_wrongPassword_throws() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPasswordHash())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(userId, "wrongPassword"));
        assertTrue(ex.getMessage().contains("no es correcta"));
    }
}
