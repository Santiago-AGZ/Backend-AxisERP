package com.axiserp.auth.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación para gestionar tokens revocados.
 * Centraliza la lógica de blacklist de tokens y limpieza de tokens expirados.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    /**
     * Revoca un token agregándolo a la blacklist.
     *
     * @param tokenJti identificador único del token (JWT ID claim)
     * @param userId identificador del usuario propietario del token
     * @param expiresAt fecha y hora de expiración del token
     * @return TokenBlacklist guardado en la base de datos
     */
    @Transactional
    public TokenBlacklist revoke(String tokenJti, UUID userId, LocalDateTime expiresAt) {
        TokenBlacklist tokenBlacklist = new TokenBlacklist(tokenJti, userId, expiresAt);
        TokenBlacklist saved = tokenBlacklistRepositoryPort.save(tokenBlacklist);
        log.info("token_revoked jti={} user_id={} expires_at={}", tokenJti, userId, expiresAt);
        return saved;
    }

    /**
     * Verifica si un token ha sido revocado.
     *
     * @param tokenJti identificador único del token
     * @return true si el token está en la blacklist, false en caso contrario
     */
    public boolean isRevoked(String tokenJti) {
        return tokenBlacklistRepositoryPort.existsByTokenJti(tokenJti);
    }

    /**
     * Limpia periódicamente los tokens expirados de la blacklist.
     * Se ejecuta cada hora (3600000 milisegundos).
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        try {
            tokenBlacklistRepositoryPort.deleteExpired();
            log.info("token_blacklist_cleanup completed");
        } catch (Exception e) {
            log.warn("token_blacklist_cleanup failed: {}", e.getMessage(), e);
        }
    }
}
