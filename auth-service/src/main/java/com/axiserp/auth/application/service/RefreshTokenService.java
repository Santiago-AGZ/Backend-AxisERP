package com.axiserp.auth.application.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación para gestionar refresh tokens.
 * Responsable de crear, validar y revocar tokens de renovación JWT.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Value("${app-refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    /**
     * Crea un nuevo refresh token para el usuario.
     *
     * @param userId identificador del usuario
     * @param ipAddress dirección IP del cliente
     * @param userAgent agente del navegador/cliente
     * @return token generado (UUID en formato String)
     */
    public String create(UUID userId, String ipAddress, String userAgent) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusDays(refreshTokenExpiryDays);

        RefreshToken refreshToken = new RefreshToken(userId, token, expiresAt);
        refreshTokenRepositoryPort.save(refreshToken);

        log.info("refresh_token_created user_id={} expiry_days={} ip_address={} user_agent={}",
                userId, refreshTokenExpiryDays, ipAddress, userAgent);

        return token;
    }

    /**
     * Valida un refresh token verificando que exista, no esté revocado y no esté expirado.
     *
     * @param userId identificador del usuario propietario del token
     * @param token el refresh token a validar
     * @return el RefreshToken si es válido
     * @throws IllegalArgumentException si el token no existe, es inválido o está expirado
     */
    public RefreshToken validate(UUID userId, String token) {
        RefreshToken refreshToken = refreshTokenRepositoryPort
                .findByUserIdAndToken(userId, token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found or invalid"));

        if (refreshToken.isRevoked()) {
            log.warn("refresh_token_validation_failed token_revoked user_id={}", userId);
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("refresh_token_validation_failed token_expired user_id={}", userId);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        log.info("refresh_token_validated user_id={}", userId);
        return refreshToken;
    }

    /**
     * Revoca un refresh token específico.
     *
     * @param token el token a revocar
     */
    @Transactional
    public void revoke(String token) {
        refreshTokenRepositoryPort.deleteByToken(token);
        log.info("refresh_token_revoked token_id=****{}", token != null && token.length() > 4 ? token.substring(token.length() - 4) : "none");
    }

    /**
     * Revoca todos los refresh tokens asociados a un usuario.
     *
     * @param userId identificador del usuario
     */
    public void revokeByUserId(UUID userId) {
        refreshTokenRepositoryPort.deleteByUserId(userId);
        log.info("refresh_tokens_revoked_by_user user_id={}", userId);
    }

    /**
     * Limpia los refresh tokens expirados de la base de datos.
     * Se ejecuta diariamente a las 2:00 AM.
     */
    @Scheduled(cron = "0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("cleanup_expired_refresh_tokens_started");
        try {
            refreshTokenRepositoryPort.deleteExpired();
            log.info("cleanup_expired_refresh_tokens_completed");
        } catch (Exception e) {
            log.warn("cleanup_expired_refresh_tokens_failed error={}", e.getMessage(), e);
        }
    }
}
