package com.axiserp.auth.application.usecase;

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

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditService auditService;

    @Mock
    private Claims claims;

    @InjectMocks
    private LogoutUseCaseImpl logoutUseCase;

    private String refreshTokenValue;
    private UUID userId;

    @BeforeEach
    void setUp() {
        refreshTokenValue = "valid-refresh-token";
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should logout successfully with valid token")
    void logout_success() {
        when(jwtService.parseToken(refreshTokenValue)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());

        RefreshToken storedToken = RefreshToken.builder()
                .userId(userId)
                .token(refreshTokenValue)
                .status(RefreshToken.TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepositoryPort.findByToken(refreshTokenValue)).thenReturn(Optional.of(storedToken));

        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        logoutUseCase.logout(refreshTokenValue);

        verify(refreshTokenRepositoryPort).save(argThat(t -> t.getStatus() == RefreshToken.TokenStatus.REVOKED));
        verify(tokenBlacklistRepositoryPort).save(any());
        verify(auditService).logLogout(eq(userId), eq("Test User"), isNull(), isNull());
    }

    @Test
    @DisplayName("Should handle invalid token gracefully")
    void logout_invalidToken() {
        when(jwtService.parseToken(refreshTokenValue)).thenThrow(new RuntimeException("Invalid token"));

        logoutUseCase.logout(refreshTokenValue);

        verify(refreshTokenRepositoryPort, never()).save(any());
        verify(tokenBlacklistRepositoryPort, never()).save(any());
    }
}
