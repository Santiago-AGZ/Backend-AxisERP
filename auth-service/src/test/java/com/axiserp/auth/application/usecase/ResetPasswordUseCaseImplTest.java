package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.PasswordHistory;
import com.axiserp.auth.domain.model.PasswordResetToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.TokenExpiredException;
import com.axiserp.auth.ports.output.PasswordHistoryRepositoryPort;
import com.axiserp.auth.ports.output.PasswordResetTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private PasswordHistoryRepositoryPort passwordHistoryRepositoryPort;

    @InjectMocks
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private String token;
    private UUID userId;

    @BeforeEach
    void setUp() {
        token = "reset-token-uuid";
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should reset password with valid token")
    void resetPassword_success() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$oldHash")
                .roleId(UUID.randomUUID())
                .status(User.UserStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordResetTokenRepositoryPort.findByToken(token)).thenReturn(resetToken);
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@1234")).thenReturn("$2a$10$newHash");
        when(passwordEncoder.matches(anyString(), eq(user.getPasswordHash()))).thenReturn(false);
        when(passwordHistoryRepositoryPort.findLastByUserId(any(), anyInt())).thenReturn(java.util.List.of());
        when(passwordResetTokenRepositoryPort.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordHistoryRepositoryPort.save(any(PasswordHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        resetPasswordUseCase.resetPassword(token, "NewPass@1234");

        verify(userRepositoryPort).save(argThat(u -> u.getPasswordHash().equals("$2a$10$newHash")));
        verify(passwordResetTokenRepositoryPort).save(argThat(t -> t.isUsed() && t.getUsedAt() != null));
        verify(passwordHistoryRepositoryPort).save(any(PasswordHistory.class));
        verify(auditService).log(any(), eq("AUTH"), eq(userId), eq(userId), eq("Test User"), any(), isNull(), isNull());
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for null token")
    void resetPassword_nullToken() {
        when(passwordResetTokenRepositoryPort.findByToken("invalid-token")).thenReturn(null);

        assertThrows(TokenExpiredException.class, () -> resetPasswordUseCase.resetPassword("invalid-token", "NewPass@1234"));
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for used token")
    void resetPassword_usedToken() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .used(true)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(passwordResetTokenRepositoryPort.findByToken(token)).thenReturn(resetToken);

        assertThrows(TokenExpiredException.class, () -> resetPasswordUseCase.resetPassword(token, "NewPass@1234"));
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired token")
    void resetPassword_expiredToken() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .used(false)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        when(passwordResetTokenRepositoryPort.findByToken(token)).thenReturn(resetToken);

        assertThrows(TokenExpiredException.class, () -> resetPasswordUseCase.resetPassword(token, "NewPass@1234"));
    }
}
