package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.dto.request.LoginRequest;
import com.axiserp.auth.application.dto.request.PasswordResetRequest;
import com.axiserp.auth.application.dto.request.ResetPasswordRequest;
import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.dto.response.UserInfoResponse;
import com.axiserp.auth.ports.input.AuthenticateUserUseCase;
import com.axiserp.auth.ports.input.GetUserInfoUseCase;
import com.axiserp.auth.ports.input.RequestPasswordResetUseCase;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
        pattern = "com\\.axiserp\\.auth\\.infrastructure\\..*Filter"))
@Import(TestSecurityConfig.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private AuthenticateUserUseCase authenticateUserUseCase;
    @MockBean private GetUserInfoUseCase getUserInfoUseCase;
    @MockBean private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockBean private ResetPasswordUseCase resetPasswordUseCase;

    @Test
    @DisplayName("POST /api/v1/auth/login - 200 OK")
    void login_success() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .name("Test User")
                .role("ADMIN")
                .build();

        when(authenticateUserUseCase.authenticate(isA(LoginRequest.class), anyString(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@axiserp.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 400 Bad Request when body is empty")
    void login_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - 200 OK")
    void me_success() throws Exception {
        UserInfoResponse response = UserInfoResponse.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@axiserp.com")
                .role("ADMIN")
                .status("ACTIVO")
                .build();

        when(getUserInfoUseCase.getUserInfo(anyString())).thenReturn(response);

        var auth = new UsernamePasswordAuthenticationToken(
                "test-user-id", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/api/v1/auth/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@axiserp.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/password-reset - 200 OK")
    void passwordReset_success() throws Exception {
        doNothing().when(requestPasswordResetUseCase).requestReset(anyString());

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@axiserp.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/password-reset - 400 Bad Request when email is missing")
    void passwordReset_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/password-reset/confirm - 200 OK")
    void confirmPasswordReset_success() throws Exception {
        doNothing().when(resetPasswordUseCase).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"valid-token\",\"newPassword\":\"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/password-reset/confirm - 400 Bad Request when fields are missing")
    void confirmPasswordReset_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
