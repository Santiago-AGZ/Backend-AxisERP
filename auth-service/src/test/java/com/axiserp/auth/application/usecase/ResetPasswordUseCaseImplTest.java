package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axiserp.auth.domain.exception.WeakPasswordException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    @Test
    @DisplayName("Should update local password hash on successful Supabase reset")
    void resetPassword_success() {
        String email = "test@axiserp.com";
        String newPassword = "StrongP@ss1";
        String encodedPassword = "encoded-password";
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash("old-hash")
                .build();

        when(supabaseAuthPort.resetPassword("supabase-recovery-token", newPassword)).thenReturn(email);
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        assertDoesNotThrow(() -> resetPasswordUseCase.resetPassword("supabase-recovery-token", newPassword));

        verify(supabaseAuthPort).resetPassword("supabase-recovery-token", newPassword);
        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepositoryPort).save(argThat(user -> encodedPassword.equals(user.getPasswordHash())));
    }

    @Test
    @DisplayName("Should handle missing local user gracefully")
    void resetPassword_localUserNotFound() {
        String email = "test@axiserp.com";

        when(supabaseAuthPort.resetPassword(anyString(), anyString())).thenReturn(email);
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> resetPasswordUseCase.resetPassword("token", "StrongP@ss1"));

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw WeakPasswordException for weak password")
    void resetPassword_weakPassword() {
        assertThrows(WeakPasswordException.class,
                () -> resetPasswordUseCase.resetPassword("token", "123"));

        verifyNoInteractions(supabaseAuthPort, userRepositoryPort, passwordEncoder);
    }

    @Test
    @DisplayName("Should throw WeakPasswordException for password without uppercase")
    void resetPassword_noUppercase() {
        assertThrows(WeakPasswordException.class,
                () -> resetPasswordUseCase.resetPassword("token", "abcdefg1@"));

        verifyNoInteractions(supabaseAuthPort, userRepositoryPort, passwordEncoder);
    }

    @Test
    @DisplayName("Should propagate Supabase error")
    void resetPassword_supabaseError() {
        when(supabaseAuthPort.resetPassword("token", "StrongP@ss1"))
                .thenThrow(new RuntimeException("Supabase error"));

        assertThrows(RuntimeException.class,
                () -> resetPasswordUseCase.resetPassword("token", "StrongP@ss1"));

        verifyNoInteractions(userRepositoryPort, passwordEncoder);
    }
}
