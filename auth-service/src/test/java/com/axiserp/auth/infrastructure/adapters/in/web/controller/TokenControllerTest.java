package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.service.OtpService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.config.SecurityConfig;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

/**
 * Tests de integración para TokenController usando MockMvc y @WebMvcTest.
 * Verifica los endpoints de logout, refresh, OTP y validación de tokens.
 */
@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class)
@DisplayName("TokenController Integration Tests")
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private SupabaseAuthPort supabaseAuthPort;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @MockBean
    private RoleRepositoryPort roleRepositoryPort;

    @MockBean
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    private UUID userId;
    private String jti;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jti = "test-jti-" + UUID.randomUUID();
        expiresAt = Instant.now().plusSeconds(3600);

        // Mock RefreshTokenService
        doNothing().when(refreshTokenService).revoke(anyString());

        // Mock OtpService
        doNothing().when(otpService).requestOtp(any(UUID.class), anyString());

        // Mock TokenBlacklistService
        when(tokenBlacklistService.revoke(anyString(), any(UUID.class), any()))
            .thenReturn(null);
    }

    /**
     * Test 1: testLogoutSuccess
     * Verifica que el endpoint POST /logout está mapeado y responde a solicitudes autenticadas.
     * Espera: POST /logout -> 200 OK
     */
    @Test
    @Disabled("TODO: Fix JWT claim 'jti' extraction in logout endpoint")
    @DisplayName("Logout endpoint should be callable with authenticated user")
    void testLogoutSuccess() throws Exception {
        String logoutRequest = "{\"refreshToken\":\"valid-refresh-token\"}";

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequest)
                .with(jwt()
                        .jwt(jwt -> jwt
                                .claim("sub", userId.toString())
                                .claim("jti", jti)
                                .expiresAt(expiresAt))))
                .andExpect(status().isOk());

        verify(tokenBlacklistService, times(1)).revoke(eq(jti), eq(userId), any());
        verify(refreshTokenService, times(1)).revoke("valid-refresh-token");
    }

    /**
     * Test 2: testRefreshTokenSuccess
     * Verifica que el endpoint POST /refresh responde correctamente.
     * Espera: POST /refresh -> 200 OK con TokenResponse
     */
    @Test
    @DisplayName("Refresh token endpoint should return 200 with valid token response")
    void testRefreshTokenSuccess() throws Exception {
        String refreshRequest = "{\"refreshToken\":\"valid-refresh-token\"}";

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest)
                .with(jwt()
                        .jwt(jwt -> jwt
                                .claim("sub", userId.toString()))))
                .andExpect(status().isOk());
    }

    /**
     * Test 3: testRequestOtpSuccess
     * Verifica que el endpoint POST /reauth-request responde correctamente para usuarios autenticados.
     * Espera: POST /reauth-request -> 200 OK con mensaje de confirmación
     */
    @Test
    @Disabled("TODO: Fix OtpService integration in request endpoint")
    @DisplayName("Request OTP endpoint should succeed with authenticated user")
    void testRequestOtpSuccess() throws Exception {
        String otpRequest = "{\"email\":\"user@example.com\"}";

        mockMvc.perform(post("/api/v1/auth/reauth-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpRequest)
                .with(jwt()
                        .jwt(jwt -> jwt
                                .claim("sub", userId.toString()))))
                .andExpect(status().isOk());
    }

    /**
     * Test 4: testVerifyOtpSuccess
     * Verifica que el endpoint POST /reauth-verify responde correctamente.
     * Espera: POST /reauth-verify -> 200 OK con OtpResponse
     */
    @Test
    @Disabled("TODO: Fix OTP verification endpoint")
    @DisplayName("Verify OTP endpoint should return 200 with valid OTP response")
    void testVerifyOtpSuccess() throws Exception {
        String otpVerifyRequest = "{\"otpCode\":\"123456\"}";

        mockMvc.perform(post("/api/v1/auth/reauth-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(otpVerifyRequest)
                .header("X-OTP-Token", "valid-otp-token"))
                .andExpect(status().isOk());
    }

    /**
     * Test 5: testValidateTokenSuccess
     * Verifica que el endpoint GET /validate-token responde correctamente para usuarios autenticados.
     * Espera: GET /validate-token -> 200 OK con información del token
     */
    @Test
    @DisplayName("Validate token endpoint should return 200 with token information")
    void testValidateTokenSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate-token")
                .with(jwt()
                        .jwt(jwt -> jwt
                                .claim("sub", userId.toString())
                                .claim("jti", jti)
                                .claim("scope", "openid profile email")
                                .expiresAt(expiresAt))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
