package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.ports.input.ActivateUserUseCase;
import com.axiserp.auth.ports.input.CreateUserUseCase;
import com.axiserp.auth.ports.input.DeactivateUserUseCase;
import com.axiserp.auth.ports.input.DeleteUserUseCase;
import com.axiserp.auth.ports.input.GetAuditLogUseCase;
import com.axiserp.auth.ports.input.GetProfileUseCase;
import com.axiserp.auth.ports.input.GetUserUseCase;
import com.axiserp.auth.ports.input.ListUsersUseCase;
import com.axiserp.auth.ports.input.ReactivateUserUseCase;
import com.axiserp.auth.ports.input.UpdateUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
        pattern = "com\\.axiserp\\.auth\\.infrastructure\\..*Filter"))
@Import(TestSecurityConfig.class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private ActivateUserUseCase activateUserUseCase;
    @MockitoBean private CreateUserUseCase createUserUseCase;
    @MockitoBean private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean private DeleteUserUseCase deleteUserUseCase;
    @MockitoBean private GetAuditLogUseCase getAuditLogUseCase;
    @MockitoBean private GetProfileUseCase getProfileUseCase;
    @MockitoBean private GetUserUseCase getUserUseCase;
    @MockitoBean private ListUsersUseCase listUsersUseCase;
    @MockitoBean private ReactivateUserUseCase reactivateUserUseCase;
    @MockitoBean private UpdateUserUseCase updateUserUseCase;

    private UserResponse mockUser() {
        return UserResponse.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@axiserp.com")
                .role("ADMIN")
                .status("ACTIVO")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/usuarios - 201 created")
    void createUser() throws Exception {
        when(createUserUseCase.create(any(CreateUserRequest.class), any(UUID.class)))
                .thenReturn(mockUser());

        var auth = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(post("/api/v1/usuarios")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateUserRequest("New User", "new@axiserp.com", "ADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios - 200")
    void listUsers() throws Exception {
        when(listUsersUseCase.listAll(any(), any(), any())).thenReturn(List.of(mockUser()));

        var auth = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(get("/api/v1/usuarios").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
