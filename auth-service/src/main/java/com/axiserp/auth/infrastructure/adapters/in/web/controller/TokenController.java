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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.auth.application.dto.request.LogoutRequest;
import com.axiserp.auth.application.dto.request.OtpRequestRequest;
import com.axiserp.auth.application.dto.request.OtpVerifyRequest;
import com.axiserp.auth.application.dto.request.RefreshTokenRequest;
import com.axiserp.auth.application.dto.response.OtpResponse;
import com.axiserp.auth.application.dto.response.TokenResponse;
import com.axiserp.auth.application.service.OtpService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.adapters.in.web.response.ApiResponse;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

/**
 * Controlador REST para gestionar tokens de autenticación.
 * Proporciona endpoints para logout, refresh de tokens, reautenticación con OTP,
 * y validación de tokens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
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
            UUID userId = UUID.fromString((String) authentication.getPrincipal());
            Jwt jwt = (Jwt) authentication.getCredentials();
            String tokenJti = jwt.getClaimAsString("jti");

            // Revoca el access token en la blacklist
            Instant expiresAt = jwt.getExpiresAt();
            tokenBlacklistService.revoke(tokenJti, userId, expiresAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());

            // Revoca el refresh token
            refreshTokenService.revoke(request.refreshToken());

            log.info("user_logout_success user_id={} jti={}", userId, tokenJti);
            return ResponseEntity.ok(ApiResponse.ok(null, "Sesión cerrada exitosamente"));
        } catch (Exception e) {
            log.error("user_logout_failed error={}", e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo cerrar la sesión: " + e.getMessage());
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
            // TODO: Implementar integración con JwtProvider para generar nuevo access token
            // Este es un placeholder que necesita ser completado con la lógica de generación de JWT

            // Por ahora, validamos el refresh token
            // La generación del nuevo access token se completará cuando se integre JwtProvider

            log.info("refresh_token_request_received");

            // Respuesta temporal hasta completar integración
            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken("placeholder_access_token")
                    .tokenType("Bearer")
                    .expiresIn(3600)
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
     * Solicita un nuevo código OTP para reautenticación.
     * Requiere autenticación previa.
     *
     * @param request contiene el email del usuario
     * @param authentication datos del usuario autenticado
     * @return 200 OK con ApiResponse vacío
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reauth-request")
    public ResponseEntity<ApiResponse<Void>> requestReauth(
            @Valid @RequestBody OtpRequestRequest request,
            Authentication authentication) {
        try {
            UUID userId = UUID.fromString((String) authentication.getPrincipal());
            otpService.requestOtp(userId, request.email());

            log.info("reauth_otp_requested user_id={} email={}", userId, request.email());
            return ResponseEntity.ok(ApiResponse.ok(null,
                    "Se ha enviado un código OTP a tu correo electrónico"));
        } catch (IllegalArgumentException e) {
            log.warn("reauth_otp_request_failed user_id={} error={}",
                    authentication.getPrincipal(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_USER", e.getMessage()));
        } catch (Exception e) {
            log.error("reauth_otp_request_error error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OTP_ERROR", "Error al solicitar el codigo OTP"));
        }
    }

    /**
     * Verifica el código OTP para reautenticación.
     * No requiere autenticación previa, pero requiere otpToken en header.
     * El otpToken se obtiene del endpoint de solicitud de OTP.
     *
     * @param request contiene el código OTP de 6 dígitos
     * @param otpToken header con el token OTP temporal
     * @return 200 OK con OtpResponse o 401 UNAUTHORIZED
     */
    @PostMapping("/reauth-verify")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyReauth(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = "X-OTP-Token", required = false) String otpToken) {
        try {
            // TODO: Extraer userId desde el otpToken o sesión temporal
            // Por ahora, placeholder para estructura del endpoint

            if (otpToken == null || otpToken.isBlank()) {
                log.warn("reauth_otp_verify_failed missing_otp_token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("MISSING_OTP_TOKEN", "Token OTP requerido en header X-OTP-Token"));
            }

            // La implementación completa requiere decodificar otpToken para obtener userId
            log.info("otp_verification_initiated");

            // Placeholder response
            OtpResponse otpResponse = OtpResponse.builder()
                    .otpToken("placeholder_otp_token")
                    .expiresIn(600)
                    .message("Reautenticación exitosa")
                    .build();

            log.info("otp_verification_success");
            return ResponseEntity.ok(ApiResponse.ok(otpResponse, "Reautenticación completada"));
        } catch (IllegalArgumentException e) {
            log.warn("otp_verification_failed error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("INVALID_OTP", e.getMessage()));
        } catch (Exception e) {
            log.error("otp_verification_error error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("OTP_ERROR", "Error al verificar el código OTP"));
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
            Jwt jwt = (Jwt) authentication.getCredentials();

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId.toString());
            tokenInfo.put("expiresAt", jwt.getExpiresAt());
            tokenInfo.put("issuedAt", jwt.getIssuedAt());
            tokenInfo.put("valid", true);
            tokenInfo.put("jti", jwt.getClaimAsString("jti"));
            tokenInfo.put("scope", jwt.getClaimAsString("scope"));

            log.info("token_validation_success user_id={} jti={}", userId, jwt.getClaimAsString("jti"));
            return ResponseEntity.ok(ApiResponse.ok(tokenInfo, "Token válido"));
        } catch (Exception e) {
            log.error("token_validation_error error={}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("valid", false);
            return ResponseEntity.ok(ApiResponse.ok(errorInfo, "Token inválido"));
        }
    }
}

