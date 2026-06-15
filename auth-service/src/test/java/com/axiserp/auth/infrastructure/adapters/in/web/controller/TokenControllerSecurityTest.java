package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.infrastructure.security.JwtAuthenticationFilter;
import com.axiserp.auth.ports.input.RefreshTokenUseCase;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

@WebMvcTest(controllers = TokenController.class)
@Import(SecurityTestConfig.class)
@DisplayName("TokenController Security")
class TokenControllerSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private RefreshTokenService refreshTokenService;
    @MockitoBean private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean private TokenBlacklistService tokenBlacklistService;
    @MockitoBean private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;


    @Test @DisplayName("POST /api/v1/auth/refresh public 200")
    void refresh_public() throws Exception {
        when(refreshTokenUseCase.refresh(any(), any(), any()))
            .thenReturn(com.axiserp.auth.application.dto.response.LoginResponse.builder().accessToken("t").refreshToken("r").build());
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"r\"}"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser
    @DisplayName("POST /api/v1/auth/logout isAuthenticated 200")
    void logout_authenticated() throws Exception {
        doNothing().when(refreshTokenService).revoke(anyString());
        when(tokenBlacklistService.revoke(anyString(), any(), any())).thenReturn(null);
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"r\"}"))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/v1/auth/logout UNAUTH 401")
    void logout_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"r\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser
    @DisplayName("GET /api/v1/auth/validate-token isAuthenticated 200")
    void validateToken_authenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate-token")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/auth/validate-token UNAUTH 401")
    void validateToken_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate-token")).andExpect(status().isUnauthorized());
    }
}
