package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.request.LoginRequest;
import com.axiserp.auth.application.dto.response.LoginResponse;

/**
 * Input port para el caso de uso de autenticación de usuarios.
 * Implementa HU-001: Iniciar sesión en el sistema.
 */
public interface AuthenticateUserUseCase {

    /**
     * Autentica un usuario con sus credenciales y genera tokens JWT.
     *
     * @param request credenciales del usuario (email y password)
     * @param ipAddress dirección IP del cliente para auditoría
     * @param userAgent agente de navegador del cliente
     * @return respuesta con access token, refresh token, rol y nombre
     * @throws com.axiserp.auth.exception.InvalidCredentialsException si las credenciales son inválidas
     * @throws com.axiserp.auth.exception.UserInactiveException si el usuario está inactivo
     */
    LoginResponse authenticate(LoginRequest request, String ipAddress, String userAgent);
}
