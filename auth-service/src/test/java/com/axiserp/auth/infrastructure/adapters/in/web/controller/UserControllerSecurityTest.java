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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.shared.PageResult;
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

@WebMvcTest(controllers = UserController.class)
@Import(SecurityTestConfig.class)
@DisplayName("UserController Security")
class UserControllerSecurityTest {

    @Autowired private MockMvc mockMvc;

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
        return UserResponse.builder().id(UUID.randomUUID()).name("T").email("e@e.com").role("ADMIN").status("ACTIVO").build();
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/usuarios ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createUserUseCase.create(any(), any())).thenReturn(mockUser());
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"n@n.com\",\"role\":\"ADMIN\"}"))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/usuarios VENDEDOR 403")
    void create_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"n@n.com\",\"role\":\"VENDEDOR\"}"))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/usuarios UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"N\",\"email\":\"n@n.com\",\"role\":\"VENDEDOR\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("GET /api/v1/usuarios ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listUsersUseCase.listAll(any(), any(), any())).thenReturn(List.of(mockUser()));
        mockMvc.perform(get("/api/v1/usuarios")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR") @DisplayName("GET /api/v1/usuarios VENDEDOR 403")
    void list_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("GET /api/v1/usuarios/deleted ADMIN 200")
    void deleted_asAdmin() throws Exception {
        when(listUsersUseCase.listAll(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/usuarios/deleted")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("GET /api/v1/usuarios/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getUserUseCase.getById(any())).thenReturn(mockUser());
        mockMvc.perform(get("/api/v1/usuarios/{id}", UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR") @DisplayName("GET /api/v1/usuarios/{id} VENDEDOR 403")
    void get_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/{id}", UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("PUT /api/v1/usuarios/{id} ADMIN 200")
    void update_asAdmin() throws Exception {
        when(updateUserUseCase.update(any(), any(), any())).thenReturn(mockUser());
        mockMvc.perform(put("/api/v1/usuarios/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"N\",\"email\":\"e@e.com\",\"role\":\"ADMIN\"}"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR") @DisplayName("PUT /api/v1/usuarios/{id} VENDEDOR 403")
    void update_asVendedor() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"N\",\"email\":\"e@e.com\",\"role\":\"VENDEDOR\"}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("PATCH /api/v1/usuarios/{id}/desactivar ADMIN 200")
    void deactivate_asAdmin() throws Exception {
        when(deactivateUserUseCase.deactivate(any(), any(), any())).thenReturn(mockUser());
        mockMvc.perform(patch("/api/v1/usuarios/{id}/desactivar", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"p\"}"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("PATCH /api/v1/usuarios/{id}/activar ADMIN 200")
    void activate_asAdmin() throws Exception {
        when(activateUserUseCase.activate(any(), any())).thenReturn(mockUser());
        mockMvc.perform(patch("/api/v1/usuarios/{id}/activar", UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("PATCH /api/v1/usuarios/{id}/reactivar ADMIN 200")
    void reactivate_asAdmin() throws Exception {
        when(reactivateUserUseCase.reactivate(any(), any())).thenReturn(mockUser());
        mockMvc.perform(patch("/api/v1/usuarios/{id}/reactivar", UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test @WithCustomUser
    @DisplayName("GET /api/v1/usuarios/me isAuthenticated 200")
    void me_authenticated() throws Exception {
        when(getProfileUseCase.getProfile(anyString())).thenReturn(mockUser());
        mockMvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/usuarios/me UNAUTH 401")
    void me_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("DELETE /api/v1/usuarios/{id} ADMIN 200")
    void delete_asAdmin() throws Exception {
        when(deleteUserUseCase.delete(any(), any(), any())).thenReturn(mockUser());
        mockMvc.perform(delete("/api/v1/usuarios/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"p\"}"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR") @DisplayName("DELETE /api/v1/usuarios/{id} VENDEDOR 403")
    void delete_asVendedor() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"p\"}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN") @DisplayName("GET /api/v1/audit-log ADMIN 200")
    void auditLog_asAdmin() throws Exception {
        when(getAuditLogUseCase.getAuditLogs(any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(new PageResult<>(List.of(), 0, 0, 0L));
        mockMvc.perform(get("/api/v1/audit-log")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR") @DisplayName("GET /api/v1/audit-log VENDEDOR 403")
    void auditLog_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")).andExpect(status().isForbidden());
    }
}
