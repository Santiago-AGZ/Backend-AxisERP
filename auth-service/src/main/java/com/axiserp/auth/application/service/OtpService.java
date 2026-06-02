package com.axiserp.auth.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.axiserp.auth.domain.model.OtpToken;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.OtpTokenRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación para gestionar códigos OTP (One-Time Password).
 * Responsable de generar, validar y enviar códigos OTP para reautenticación.
 * Los OTP tienen una validez de 10 minutos y pueden utilizarse una sola vez.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpTokenRepositoryPort otpTokenRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SupabaseAuthPort supabaseAuthPort;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    /**
     * Solicita un nuevo código OTP para el usuario especificado.
     * Genera un código de 6 dígitos, lo hashea, lo guarda y envía por email.
     *
     * @param userId identificador del usuario
     * @param email correo electrónico del usuario
     * @throws IllegalArgumentException si el usuario no existe
     */
    public void requestOtp(UUID userId, String email) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Elimina OTP previo si existe
        otpTokenRepositoryPort.deleteByUserId(userId);

        // Genera código OTP
        String otpCode = generateOtpCode();
        String hashedCode = hashOtpCode(otpCode);

        // Crea token OTP
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        OtpToken otpToken = new OtpToken(userId, hashedCode, expiresAt);

        // Guarda en base de datos
        otpTokenRepositoryPort.save(otpToken);

        // Envía email con OTP
        sendOtpEmail(email, otpCode, user.getName());

        log.info("otp_requested user_id={} email={} expiry_minutes={}", userId, email, otpExpiryMinutes);
    }

    /**
     * Valida un código OTP para el usuario especificado.
     * Verifica que el código sea correcto, no esté expirado y no haya sido usado.
     *
     * @param userId identificador del usuario propietario del OTP
     * @param otpCode código OTP a validar (6 dígitos)
     * @return token OTP válido para usar en reautenticación
     * @throws IllegalArgumentException si el OTP no existe, es inválido, está expirado o fue usado
     */
    public String verifyOtp(UUID userId, String otpCode) {
        OtpToken otpToken = otpTokenRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("OTP no solicitado o no válido"));

        // Verifica si está expirado
        if (otpToken.isExpired()) {
            log.warn("otp_validation_failed token_expired user_id={}", userId);
            throw new IllegalArgumentException("El código OTP ha expirado");
        }

        // Verifica si ya fue usado
        if (otpToken.isUsed()) {
            log.warn("otp_validation_failed token_already_used user_id={}", userId);
            throw new IllegalArgumentException("El código OTP ya fue utilizado");
        }

        // Verifica el código
        String hashedInput = hashOtpCode(otpCode);
        if (!hashedInput.equals(otpToken.getToken())) {
            log.warn("otp_validation_failed invalid_code user_id={}", userId);
            throw new IllegalArgumentException("El código OTP es incorrecto");
        }

        // Marca como usado
        otpToken.markAsUsed();
        otpTokenRepositoryPort.save(otpToken);

        // Genera token de sesión para reautenticación
        String reauthToken = generateOtpToken();

        log.info("otp_verified user_id={}", userId);
        return reauthToken;
    }

    /**
     * Genera un código OTP aleatorio de 6 dígitos.
     *
     * @return código OTP como String (ej: "123456")
     */
    private String generateOtpCode() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Genera un token seguro para usar en reautenticación post-OTP.
     * Utiliza UUID en formato Base64.
     *
     * @return token seguro
     */
    private String generateOtpToken() {
        String uuid = UUID.randomUUID().toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.getBytes());
    }

    /**
     * Hashea un código OTP usando SHA-256.
     *
     * @param otpCode código a hashear
     * @return código hasheado en formato hexadecimal
     */
    private String hashOtpCode(String otpCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    /**
     * Envía un código OTP por email al usuario.
     * Nota: Esta es una implementación stub que se integraría con un servicio de email.
     *
     * @param email correo del usuario
     * @param otpCode código OTP a enviar
     * @param userName nombre del usuario
     */
    private void sendOtpEmail(String email, String otpCode, String userName) {
        // Implementación: llamar a servicio de email (Supabase, SendGrid, etc.)
        log.info("otp_email_sent email={} user_name={}", email, userName);

        // TODO: Implementar integración con servicio de email
        // mailService.sendOtpEmail(email, otpCode, userName);
    }

    /**
     * Limpia los códigos OTP expirados de la base de datos.
     * Se ejecuta cada 15 minutos.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void cleanupExpiredTokens() {
        log.info("cleanup_expired_otp_tokens_started");
        try {
            otpTokenRepositoryPort.deleteExpired();
            log.info("cleanup_expired_otp_tokens_completed");
        } catch (Exception e) {
            log.warn("cleanup_expired_otp_tokens_failed error={}", e.getMessage(), e);
        }
    }
}
