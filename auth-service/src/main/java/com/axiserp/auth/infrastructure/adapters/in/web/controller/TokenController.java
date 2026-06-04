package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.LogoutRequest;
import com.axiserp.auth.application.dto.request.RefreshTokenRequest;
import com.axiserp.auth.application.dto.response.TokenResponse;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort.RefreshTokenResponse;

/**
 * Controlador REST para gestionar tokens de autenticación.
 * Proporciona endpoints para logout, refresh de tokens y validación de tokens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SupabaseAuthPort supabaseAuthPort;

    /**
     * Realiza logout revocando el access token y refresh token del usuario.
     *
     * @param request contiene el refresh token a revocar
     * @param authentication datos del usuario autenticado
     * @return 200 OK con ApiResponse vacío
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            Authentication authentication) {
        try {
            String principal = authentication.getPrincipal().toString();
            UUID userId = UUID.fromString(principal);

            // Blacklist access token JTI if available (OAuth2 flow)
            if (authentication.getCredentials() instanceof Jwt jwt) {
                String tokenJti = jwt.getClaimAsString("jti");
                if (tokenJti != null) {
                    Instant expiresAt = jwt.getExpiresAt();
                    if (expiresAt != null) {
                        tokenBlacklistService.revoke(tokenJti, userId,
                                expiresAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    }
                }
            }

            // Revoca el refresh token
            refreshTokenService.revoke(request.refreshToken());

            log.info("user_logout_success user_id={}", userId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Sesion cerrada exitosamente"));
        } catch (Exception e) {
            log.error("user_logout_failed error={}", e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo cerrar la sesion: " + e.getMessage());
        }
    }

    /**
     * Refresca el access token utilizando un refresh token válido.
     * No requiere autenticación previa.
     *
     * @param request contiene el refresh token
     * @return 200 OK con TokenResponse o 401 UNAUTHORIZED
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse supabaseResponse = supabaseAuthPort.refreshToken(
                    request.refreshToken());

            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(supabaseResponse.accessToken())
                    .tokenType("Bearer")
                    .expiresIn(supabaseResponse.expiresIn())
                    .build();

            log.info("access_token_refreshed");
            return ResponseEntity.ok(ApiResponse.ok(tokenResponse, "Token renovado exitosamente"));
        } catch (IllegalArgumentException e) {
            log.warn("refresh_token_validation_failed error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("INVALID_TOKEN", "El refresh token es invalido o ha expirado"));
        } catch (Exception e) {
            log.error("refresh_token_error error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("TOKEN_ERROR", "Error al renovar el token"));
        }
    }

    /**
     * Valida el token actual del usuario autenticado.
     * Retorna información del token (userId, expiración, validez).
     *
     * @param authentication datos del usuario autenticado
     * @return 200 OK con mapa de información del token
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            Authentication authentication) {
        try {
            UUID userId = UUID.fromString((String) authentication.getPrincipal());

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId.toString());
            tokenInfo.put("valid", true);

            if (authentication.getCredentials() instanceof Jwt jwt) {
                tokenInfo.put("expiresAt", jwt.getExpiresAt());
                tokenInfo.put("issuedAt", jwt.getIssuedAt());
                tokenInfo.put("jti", jwt.getClaimAsString("jti"));
                tokenInfo.put("scope", jwt.getClaimAsString("scope"));
            }

            log.info("token_validation_success user_id={}", userId);
            return ResponseEntity.ok(ApiResponse.ok(tokenInfo, "Token válido"));
        } catch (Exception e) {
            log.error("token_validation_error error={}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("valid", false);
            return ResponseEntity.ok(ApiResponse.ok(errorInfo, "Token inválido"));
        }
    }
}

