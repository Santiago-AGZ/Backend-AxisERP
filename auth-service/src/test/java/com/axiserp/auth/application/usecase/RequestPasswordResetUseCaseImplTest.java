package com.axiserp.auth.application.usecase;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.PasswordResetToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.EmailSenderPort;
import com.axiserp.auth.ports.output.PasswordResetTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RequestPasswordResetUseCaseImpl requestPasswordResetUseCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should request password reset for existing user")
    void requestReset_existingUser() {
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$hashed")
                .roleId(UUID.randomUUID())
                .status(User.UserStatus.ACTIVO)
                .build();

        when(userRepositoryPort.findByEmail("test@axiserp.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepositoryPort.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestPasswordResetUseCase.requestReset("test@axiserp.com");

        verify(passwordResetTokenRepositoryPort).deleteByUserId(userId);
        verify(passwordResetTokenRepositoryPort).save(argThat(token ->
                token.getUserId().equals(userId) && !token.isUsed()));
        verify(emailSenderPort).sendPasswordResetEmail(eq("test@axiserp.com"), anyString());
        verify(auditService).log(any(), eq("AUTH"), eq(userId), eq(userId), eq("Test User"), any(), isNull(), isNull());
    }

    @Test
    @DisplayName("Should return silently for non-existing user (security)")
    void requestReset_nonExistingUser() {
        when(userRepositoryPort.findByEmail("unknown@axiserp.com")).thenReturn(Optional.empty());

        requestPasswordResetUseCase.requestReset("unknown@axiserp.com");

        verify(passwordResetTokenRepositoryPort, never()).save(any());
        verify(emailSenderPort, never()).sendPasswordResetEmail(any(), any());
    }
}
