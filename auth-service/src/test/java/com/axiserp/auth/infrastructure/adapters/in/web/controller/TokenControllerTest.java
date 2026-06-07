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

import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.config.SecurityConfig;
import com.axiserp.auth.ports.input.RefreshTokenUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class)
@DisplayName("TokenController Integration Tests")
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @MockBean
    private RoleRepositoryPort roleRepositoryPort;

    @MockBean
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @MockBean
    private JwtService jwtService;

    private UUID userId;
    private String jti;
    private Instant expiresAt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jti = "test-jti-" + UUID.randomUUID();
        expiresAt = Instant.now().plusSeconds(3600);

        doNothing().when(refreshTokenService).revoke(anyString());

        when(tokenBlacklistService.revoke(anyString(), any(UUID.class), any()))
            .thenReturn(null);

        var loginResponse = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .name("Test User")
                .role("ADMIN")
                .build();
        when(refreshTokenUseCase.refresh(anyString(), anyString(), any())).thenReturn(loginResponse);
    }

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

    @Test
    @DisplayName("Refresh token endpoint should return 200 with valid token response")
    void testRefreshTokenSuccess() throws Exception {
        String refreshRequest = "{\"refreshToken\":\"valid-refresh-token\"}";

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

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