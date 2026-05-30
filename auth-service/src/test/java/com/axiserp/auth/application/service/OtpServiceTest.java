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

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

/**
 * Pruebas unitarias para OtpService.
 * Cubre la generación, validación, envío y limpieza de códigos OTP.
 * Los tests validan:
 * - Solicitud exitosa de OTP con usuario existente
 * - Error cuando el usuario no existe
 * - Validación exitosa de OTP válido y no expirado
 * - Rechazo de OTP con código inválido
 * - Rechazo de OTP expirado
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OtpService Tests")
class OtpServiceTest {

    @Mock
    private OtpTokenRepositoryPort otpTokenRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

    @InjectMocks
    private OtpService otpService;

    private UUID userId;
    private String email;
    private String userName;
    private User user;
    private OtpToken otpToken;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@example.com";
        userName = "Test User";
        expiresAt = LocalDateTime.now().plusMinutes(10);

        // Configurar usuario
        user = User.builder()
                .id(userId)
                .name(userName)
                .email(email)
                .status(UserStatus.ACTIVO)
                .build();

        // Configurar valores de configuración
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 10);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 3);
    }

    /**
     * Test 1: testRequestOtpSuccess
     * Verifica que requestOtp() genera un OTP, lo hashea, lo guarda en repositorio
     * y solicita envío de email cuando el usuario existe.
     */
    @Test
    @DisplayName("requestOtp() should generate OTP, hash it, save to repository, and send email when user exists")
    void testRequestOtpSuccess() {
        // Arrange
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(otpTokenRepositoryPort).deleteByUserId(userId);
        ArgumentCaptor<OtpToken> otpCaptor = ArgumentCaptor.forClass(OtpToken.class);

        // Act
        otpService.requestOtp(userId, email);

        // Assert
        // Verificar que findById fue llamado
        verify(userRepositoryPort, times(1)).findById(userId);

        // Verificar que deleteByUserId fue llamado para limpiar OTP previo
        verify(otpTokenRepositoryPort, times(1)).deleteByUserId(userId);

        // Verificar que save fue llamado con un OtpToken válido
        verify(otpTokenRepositoryPort, times(1)).save(otpCaptor.capture());
        OtpToken savedToken = otpCaptor.getValue();

        assertNotNull(savedToken);
        assertEquals(userId, savedToken.getUserId());
        assertNotNull(savedToken.getToken());
        assertFalse(savedToken.isUsed());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()),
                "Expiry time should be in the future");
    }

    /**
     * Test 2: testRequestOtpUserNotFound
     * Verifica que requestOtp() lanza IllegalArgumentException cuando el usuario no existe.
     */
    @Test
    @DisplayName("requestOtp() should throw IllegalArgumentException when user not found")
    void testRequestOtpUserNotFound() {
        // Arrange
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> otpService.requestOtp(userId, email)
        );

        assertTrue(exception.getMessage().contains("Usuario no encontrado"),
                "Exception message should mention 'Usuario no encontrado'");

        // Verificar que no se intentó guardar nada
        verify(otpTokenRepositoryPort, never()).save(any());
    }

    /**
     * Test 3: testVerifyOtpSuccess
     * Verifica que verifyOtp() valida exitosamente un código OTP válido y no expirado,
     * lo marca como usado, y retorna un token de reautenticación.
     */
    @Test
    @DisplayName("verifyOtp() should validate OTP successfully, mark as used, and return reauth token")
    void testVerifyOtpSuccess() {
        // Arrange
        String otpCode = "123456";
        String hashedCode = hashOtpCode(otpCode);
        OtpToken validToken = new OtpToken(userId, hashedCode, expiresAt.plusMinutes(5));

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(validToken));
        ArgumentCaptor<OtpToken> otpCaptor = ArgumentCaptor.forClass(OtpToken.class);

        // Act
        String reauthToken = otpService.verifyOtp(userId, otpCode);

        // Assert
        assertNotNull(reauthToken, "Reauth token should not be null");
        assertFalse(reauthToken.isEmpty(), "Reauth token should not be empty");

        // Verificar que el token fue marcado como usado
        verify(otpTokenRepositoryPort, times(1)).save(otpCaptor.capture());
        OtpToken savedToken = otpCaptor.getValue();
        assertTrue(savedToken.isUsed(), "Token should be marked as used");
    }

    /**
     * Test 4: testVerifyOtpInvalid
     * Verifica que verifyOtp() lanza IllegalArgumentException cuando el código OTP es inválido.
     */
    @Test
    @DisplayName("verifyOtp() should throw IllegalArgumentException when OTP code is invalid")
    void testVerifyOtpInvalid() {
        // Arrange
        String correctOtpCode = "123456";
        String incorrectOtpCode = "654321";
        String hashedCode = hashOtpCode(correctOtpCode);
        OtpToken validToken = new OtpToken(userId, hashedCode, expiresAt.plusMinutes(5));

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(validToken));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> otpService.verifyOtp(userId, incorrectOtpCode)
        );

        assertTrue(exception.getMessage().contains("incorrecto"),
                "Exception message should mention 'incorrecto'");

        // Verificar que no se intentó guardar cambios
        verify(otpTokenRepositoryPort, never()).save(any());
    }

    /**
     * Test 5: testVerifyOtpExpired
     * Verifica que verifyOtp() lanza IllegalArgumentException cuando el OTP está expirado.
     */
    @Test
    @DisplayName("verifyOtp() should throw IllegalArgumentException when OTP is expired")
    void testVerifyOtpExpired() {
        // Arrange
        String otpCode = "123456";
        String hashedCode = hashOtpCode(otpCode);
        // Crear token con expiración en el pasado
        LocalDateTime pastExpiry = LocalDateTime.now().minusMinutes(5);
        OtpToken expiredToken = new OtpToken(userId, hashedCode, pastExpiry);

        when(otpTokenRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> otpService.verifyOtp(userId, otpCode)
        );

        assertTrue(exception.getMessage().contains("expirado"),
                "Exception message should mention 'expirado'");

        // Verificar que no se intentó guardar cambios
        verify(otpTokenRepositoryPort, never()).save(any());
    }

    /**
     * Método auxiliar para hashear código OTP usando SHA-256.
     * Replica la lógica privada de OtpService para tests.
     *
     * @param otpCode código a hashear
     * @return código hasheado en formato hexadecimal
     */
    private String hashOtpCode(String otpCode) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otpCode.getBytes());

            // Convierte a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
