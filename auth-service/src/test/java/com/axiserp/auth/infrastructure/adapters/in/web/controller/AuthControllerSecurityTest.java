package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.dto.response.UserInfoResponse;
import com.axiserp.auth.infrastructure.security.JwtAuthenticationFilter;
import com.axiserp.auth.ports.input.AuthenticateUserUseCase;
import com.axiserp.auth.ports.input.GetUserInfoUseCase;
import com.axiserp.auth.ports.input.RequestPasswordResetUseCase;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityTestConfig.class)
@DisplayName("AuthController Security")
class AuthControllerSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthenticateUserUseCase authenticateUserUseCase;
    @MockitoBean private GetUserInfoUseCase getUserInfoUseCase;
    @MockitoBean private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean private ResetPasswordUseCase resetPasswordUseCase;


    @Test @DisplayName("POST /api/v1/auth/login public 200")
    void login_public() throws Exception {
        when(authenticateUserUseCase.authenticate(any(), anyString(), anyString()))
            .thenReturn(com.axiserp.auth.application.dto.response.LoginResponse.builder().accessToken("t").build());
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"a@b.com\",\"password\":\"p\"}"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser
    @DisplayName("GET /api/v1/auth/me isAuthenticated 200")
    void me_authenticated() throws Exception {
        when(getUserInfoUseCase.getUserInfo(anyString()))
            .thenReturn(UserInfoResponse.builder().id(UUID.randomUUID()).name("T").email("e@e.com").role("ADMIN").status("ACTIVO").build());
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/auth/me UNAUTH 401")
    void me_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("POST /api/v1/auth/password-reset public 200")
    void passwordReset_public() throws Exception {
        doNothing().when(requestPasswordResetUseCase).requestReset(anyString());
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType("application/json").content("{\"email\":\"a@b.com\"}"))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /api/v1/auth/password-reset/confirm public 200")
    void confirmReset_public() throws Exception {
        doNothing().when(resetPasswordUseCase).resetPassword(anyString(), anyString());
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType("application/json").content("{\"token\":\"t\",\"newPassword\":\"password123\"}"))
            .andExpect(status().isOk());
    }
}
