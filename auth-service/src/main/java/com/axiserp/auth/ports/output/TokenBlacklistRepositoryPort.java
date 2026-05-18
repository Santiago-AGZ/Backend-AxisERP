package com.axiserp.auth.ports.output;

import com.axiserp.auth.domain.model.TokenBlacklist;

/**
 * Port de salida para operaciones de persistencia de tokens en blacklist.
 */
public interface TokenBlacklistRepositoryPort {

    /**
     * Verifica si un token está en la blacklist.
     *
     * @param token valor del token a verificar
     * @return true si está en blacklist, false en caso contrario
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Agrega un token a la blacklist.
     *
     * @param tokenBlacklist dominio del token a blacklistear
     * @return registro persistido
     */
    TokenBlacklist save(TokenBlacklist tokenBlacklist);
}
