package com.axiserp.auth.application.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

/**
 * Pruebas unitarias para RefreshTokenService.
 * Cubre la creación, validación, revocación y limpieza de refresh tokens.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = UUID.randomUUID().toString();
        expiresAt = LocalDateTime.now().plusDays(7);

        // Configurar el valor de expiry days a 7 (default)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiryDays", 7);
    }

    /**
     * Test 1: testCreate
     * Verifica que create() crea un token UUID y lo guarda en el repositorio.
     */
    @Test
    @DisplayName("create() should generate UUID token and save to repository")
    void testCreate() {
        // Arrange
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        // Act
        String generatedToken = refreshTokenService.create(userId, ipAddress, userAgent);

        // Assert
        assertNotNull(generatedToken, "Generated token should not be null");
        // Verificar que el token es un UUID válido
        assertDoesNotThrow(() -> UUID.fromString(generatedToken));

        // Verificar que save() fue llamado con un RefreshToken
        verify(refreshTokenRepositoryPort, times(1)).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();

        assertEquals(userId, savedToken.getUserId());
        assertEquals(generatedToken, savedToken.getToken());
        assertFalse(savedToken.isRevoked());
        assertNotNull(savedToken.getId());
    }

    /**
     * Test 2: testValidateSuccess
     * Verifica que validate() retorna el RefreshToken cuando es válido (no expirado, no revocado).
     */
    @Test
    @DisplayName("validate() should return RefreshToken when valid (not expired, not revoked)")
    void testValidateSuccess() {
        // Arrange
        RefreshToken validToken = new RefreshToken(userId, token, expiresAt.plusDays(1));
        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(validToken));

        // Act
        RefreshToken result = refreshTokenService.validate(userId, token);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(token, result.getToken());
        assertFalse(result.isRevoked());
        assertFalse(result.isExpired());

        verify(refreshTokenRepositoryPort, times(1)).findByUserIdAndToken(userId, token);
    }

    /**
     * Test 3: testValidateExpired
     * Verifica que validate() lanza IllegalArgumentException cuando el token está expirado.
     */
    @Test
    @DisplayName("validate() should throw IllegalArgumentException when token is expired")
    void testValidateExpired() {
        // Arrange
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);
        RefreshToken expiredToken = new RefreshToken(userId, token, pastDateTime);
        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(expiredToken));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.validate(userId, token)
        );

        assertTrue(exception.getMessage().contains("expired"),
                "Exception message should mention 'expired'");

        verify(refreshTokenRepositoryPort, times(1)).findByUserIdAndToken(userId, token);
    }

    /**
     * Test 4: testRevoke
     * Verifica que revoke() llama a deleteByToken() del repositorio.
     */
    @Test
    @DisplayName("revoke() should call deleteByToken() on repository")
    void testRevoke() {
        // Arrange
        String tokenToRevoke = UUID.randomUUID().toString();
        doNothing().when(refreshTokenRepositoryPort).deleteByToken(tokenToRevoke);

        // Act
        refreshTokenService.revoke(tokenToRevoke);

        // Assert
        verify(refreshTokenRepositoryPort, times(1)).deleteByToken(tokenToRevoke);
        verifyNoMoreInteractions(refreshTokenRepositoryPort);
    }

    /**
     * Test 5: testRevokeByUserId
     * Verifica que revokeByUserId() llama a deleteByUserId() del repositorio.
     */
    @Test
    @DisplayName("revokeByUserId() should call deleteByUserId() on repository")
    void testRevokeByUserId() {
        // Arrange
        UUID userIdToRevoke = UUID.randomUUID();
        doNothing().when(refreshTokenRepositoryPort).deleteByUserId(userIdToRevoke);

        // Act
        refreshTokenService.revokeByUserId(userIdToRevoke);

        // Assert
        verify(refreshTokenRepositoryPort, times(1)).deleteByUserId(userIdToRevoke);
        verifyNoMoreInteractions(refreshTokenRepositoryPort);
    }

    /**
     * Test 6: testCleanupExpiredTokens
     * Verifica que cleanupExpiredTokens() llama a deleteExpired() del repositorio.
     */
    @Test
    @DisplayName("cleanupExpiredTokens() should call deleteExpired() on repository")
    void testCleanupExpiredTokens() {
        // Arrange
        doNothing().when(refreshTokenRepositoryPort).deleteExpired();

        // Act
        refreshTokenService.cleanupExpiredTokens();

        // Assert
        verify(refreshTokenRepositoryPort, times(1)).deleteExpired();
        verifyNoMoreInteractions(refreshTokenRepositoryPort);
    }

    /**
     * Test adicional: testValidateNotFound
     * Verifica que validate() lanza IllegalArgumentException cuando el token no existe.
     */
    @Test
    @DisplayName("validate() should throw IllegalArgumentException when token not found")
    void testValidateNotFound() {
        // Arrange
        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.validate(userId, token)
        );

        assertTrue(exception.getMessage().contains("not found"),
                "Exception message should mention 'not found'");

        verify(refreshTokenRepositoryPort, times(1)).findByUserIdAndToken(userId, token);
    }

    /**
     * Test adicional: testValidateRevoked
     * Verifica que validate() lanza IllegalArgumentException cuando el token está revocado.
     */
    @Test
    @DisplayName("validate() should throw IllegalArgumentException when token is revoked")
    void testValidateRevoked() {
        // Arrange
        RefreshToken revokedToken = new RefreshToken(userId, token, expiresAt.plusDays(1));
        revokedToken.revoke();
        when(refreshTokenRepositoryPort.findByUserIdAndToken(userId, token))
                .thenReturn(Optional.of(revokedToken));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.validate(userId, token)
        );

        assertTrue(exception.getMessage().contains("revoked"),
                "Exception message should mention 'revoked'");

        verify(refreshTokenRepositoryPort, times(1)).findByUserIdAndToken(userId, token);
    }
}
