package com.axiserp.auth.domain.service;

import com.axiserp.auth.domain.model.User;

/**
 * Estrategia de rate limiting para intentos de login.
 * Implementa el patrón Strategy para permitir diferentes
 * políticas de bloqueo (Patrón Strategy).
 */
public interface LoginRateLimitStrategy {

    int MAX_ATTEMPTS = 5;

    /**
     * Determina si el usuario puede intentar login.
     *
     * @param user usuario que intenta autenticarse
     * @return true si puede intentar, false si está bloqueado
     */
    boolean isLoginAllowed(User user);

    /**
     * Registra un intento fallido y retorna el usuario actualizado.
     *
     * @param user usuario con intento fallido
     * @return usuario con intentos actualizados (posiblemente bloqueado)
     */
    User recordFailedAttempt(User user);

    /**
     * Resetea los intentos fallidos tras login exitoso.
     *
     * @param user usuario con login exitoso
     * @return usuario con intentos reseteados
     */
    User recordSuccessfulLogin(User user);

    /**
     * Retorna los intentos restantes antes del bloqueo.
     *
     * @param user usuario actual
     * @return intentos restantes
     */
    int remainingAttempts(User user);
}
