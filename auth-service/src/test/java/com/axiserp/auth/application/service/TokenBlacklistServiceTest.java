package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Tests")
class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    private String tokenJti;
    private UUID userId;
    private LocalDateTime expiresAt;
    private TokenBlacklist tokenBlacklist;

    @BeforeEach
    void setUp() {
        tokenJti = "test-jti-12345";
        userId = UUID.randomUUID();
        expiresAt = LocalDateTime.now().plusHours(1);
        tokenBlacklist = new TokenBlacklist(tokenJti, userId, expiresAt);
    }

    @Test
    @DisplayName("testRevoke - verifica que save() es llamado con el TokenBlacklist correcto")
    void testRevoke() {
        // Arrange
        when(tokenBlacklistRepositoryPort.save(any(TokenBlacklist.class)))
                .thenReturn(tokenBlacklist);

        // Act
        TokenBlacklist result = tokenBlacklistService.revoke(tokenJti, userId, expiresAt);

        // Assert
        assertNotNull(result);
        assertEquals(tokenJti, result.getTokenJti());
        assertEquals(userId, result.getUserId());
        assertEquals(expiresAt, result.getExpiresAt());
        verify(tokenBlacklistRepositoryPort, times(1)).save(any(TokenBlacklist.class));
    }

    @Test
    @DisplayName("testIsRevokedTrue - mock retorna true cuando el token está en blacklist")
    void testIsRevokedTrue() {
        // Arrange
        when(tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti))
                .thenReturn(true);

        // Act
        boolean isRevoked = tokenBlacklistService.isRevoked(tokenJti);

        // Assert
        assertTrue(isRevoked);
        verify(tokenBlacklistRepositoryPort, times(1)).existsByTokenJti(tokenJti);
    }

    @Test
    @DisplayName("testIsRevokedFalse - mock retorna false cuando el token no está en blacklist")
    void testIsRevokedFalse() {
        // Arrange
        when(tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti))
                .thenReturn(false);

        // Act
        boolean isRevoked = tokenBlacklistService.isRevoked(tokenJti);

        // Assert
        assertFalse(isRevoked);
        verify(tokenBlacklistRepositoryPort, times(1)).existsByTokenJti(tokenJti);
    }

    @Test
    @DisplayName("testCleanupExpiredTokens - verifica que deleteExpired() es llamado")
    void testCleanupExpiredTokens() {
        // Arrange
        doNothing().when(tokenBlacklistRepositoryPort).deleteExpired();

        // Act
        tokenBlacklistService.cleanupExpiredTokens();

        // Assert
        verify(tokenBlacklistRepositoryPort, times(1)).deleteExpired();
    }
}
