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

import com.axiserp.auth.application.dto.request.CreateRoleRequest;
import com.axiserp.auth.application.dto.request.UpdateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.ports.input.CreateRoleUseCase;
import com.axiserp.auth.ports.input.DeleteRoleUseCase;
import com.axiserp.auth.ports.input.GetRoleUseCase;
import com.axiserp.auth.ports.input.ListRolesUseCase;
import com.axiserp.auth.ports.input.UpdateRoleUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = RoleController.class)
@Import(SecurityTestConfig.class)
@DisplayName("RoleController Security")
class RoleControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ListRolesUseCase listRolesUseCase;
    @MockitoBean private GetRoleUseCase getRoleUseCase;
    @MockitoBean private CreateRoleUseCase createRoleUseCase;
    @MockitoBean private UpdateRoleUseCase updateRoleUseCase;
    @MockitoBean private DeleteRoleUseCase deleteRoleUseCase;

    private static final UUID RID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private RoleResponse mockRole() {
        return RoleResponse.builder().id(RID).name("ADMIN").description("Admin").build();
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/auth/roles ADMIN 200")
    void listRoles_asAdmin() throws Exception {
        when(listRolesUseCase.listAll()).thenReturn(List.of(mockRole()));
        mockMvc.perform(get("/api/v1/auth/roles")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/auth/roles VENDEDOR 403")
    void listRoles_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles")).andExpect(status().isForbidden());
    }

    @Test @DisplayName("GET /api/v1/auth/roles UNAUTH 401")
    void listRoles_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/auth/roles/{id} ADMIN 200")
    void getRole_asAdmin() throws Exception {
        when(getRoleUseCase.getById(any())).thenReturn(mockRole());
        mockMvc.perform(get("/api/v1/auth/roles/{id}", RID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/auth/roles/{id} VENDEDOR 403")
    void getRole_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles/{id}", RID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/auth/roles ADMIN 201")
    void createRole_asAdmin() throws Exception {
        when(createRoleUseCase.create(any(), any())).thenReturn(mockRole());
        mockMvc.perform(post("/api/v1/auth/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateRoleRequest("R", "D"))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/auth/roles VENDEDOR 403")
    void createRole_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/auth/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateRoleRequest("R", "D"))))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PUT /api/v1/auth/roles/{id} ADMIN 200")
    void updateRole_asAdmin() throws Exception {
        when(updateRoleUseCase.update(any(), any(), any())).thenReturn(mockRole());
        mockMvc.perform(put("/api/v1/auth/roles/{id}", RID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest("U", "D"))))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PUT /api/v1/auth/roles/{id} VENDEDOR 403")
    void updateRole_asVendedor() throws Exception {
        mockMvc.perform(put("/api/v1/auth/roles/{id}", RID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateRoleRequest("U", "D"))))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("DELETE /api/v1/auth/roles/{id} ADMIN 200")
    void deleteRole_asAdmin() throws Exception {
        doNothing().when(deleteRoleUseCase).delete(any());
        mockMvc.perform(delete("/api/v1/auth/roles/{id}", RID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("DELETE /api/v1/auth/roles/{id} VENDEDOR 403")
    void deleteRole_asVendedor() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/roles/{id}", RID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/auth/permissions ADMIN 200")
    void permissions_asAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/auth/permissions")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/auth/permissions VENDEDOR 403")
    void permissions_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/auth/permissions")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/auth/roles/{roleId}/permissions ADMIN 200")
    void rolePermissions_asAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles/{roleId}/permissions", RID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/auth/roles/{roleId}/permissions ADMIN 200")
    void assignPermissions_asAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/roles/{roleId}/permissions", RID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("DELETE /api/v1/auth/roles/{roleId}/permissions/{permId} ADMIN 200")
    void removePermission_asAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/roles/{roleId}/permissions/{permId}", RID, RID))
            .andExpect(status().isOk());
    }
}
