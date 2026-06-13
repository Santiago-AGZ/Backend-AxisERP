package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.domain.exception.WeakPasswordException;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    @Test
    @DisplayName("Should call Supabase with valid token and strong password")
    void resetPassword_success() {
        doNothing().when(supabaseAuthPort).resetPassword("supabase-recovery-token", "StrongP@ss1");

        assertDoesNotThrow(() -> resetPasswordUseCase.resetPassword("supabase-recovery-token", "StrongP@ss1"));

        verify(supabaseAuthPort).resetPassword("supabase-recovery-token", "StrongP@ss1");
    }

    @Test
    @DisplayName("Should throw WeakPasswordException for weak password")
    void resetPassword_weakPassword() {
        assertThrows(WeakPasswordException.class,
                () -> resetPasswordUseCase.resetPassword("token", "123"));
    }

    @Test
    @DisplayName("Should throw WeakPasswordException for password without uppercase")
    void resetPassword_noUppercase() {
        assertThrows(WeakPasswordException.class,
                () -> resetPasswordUseCase.resetPassword("token", "abcdefg1@"));
    }

    @Test
    @DisplayName("Should propagate Supabase error")
    void resetPassword_supabaseError() {
        doThrow(new RuntimeException("Supabase error"))
                .when(supabaseAuthPort).resetPassword("token", "StrongP@ss1");

        assertThrows(RuntimeException.class,
                () -> resetPasswordUseCase.resetPassword("token", "StrongP@ss1"));
    }
}
