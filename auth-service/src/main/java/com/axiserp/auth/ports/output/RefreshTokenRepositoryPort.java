package com.axiserp.auth.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.auth.domain.model.RefreshToken;

/**
 * Port de salida para operaciones de persistencia de refresh tokens.
 */
public interface RefreshTokenRepositoryPort {

    /**
     * Busca un refresh token por su valor.
     *
     * @param token valor del refresh token
     * @return Optional con el token si existe, vacío si no
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca todos los refresh tokens activos de un usuario.
     *
     * @param userId identificador del usuario
     * @return lista de tokens activos
     */
    List<RefreshToken> findActiveByUserId(UUID userId);

    /**
     * Persiste un refresh token.
     *
     * @param refreshToken dominio del token a persistir
     * @return token persistido con datos generados por BD
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Revoca todos los refresh tokens activos de un usuario.
     *
     * @param userId identificador del usuario
     */
    void revokeByUserId(UUID userId);
}
