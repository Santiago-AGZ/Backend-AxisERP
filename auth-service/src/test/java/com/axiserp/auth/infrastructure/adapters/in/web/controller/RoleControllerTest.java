package com.axiserp.auth.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.axiserp.auth.application.dto.request.CreateRoleRequest;
import com.axiserp.auth.application.dto.request.UpdateRoleRequest;
import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.ports.input.CreateRoleUseCase;
import com.axiserp.auth.ports.input.DeleteRoleUseCase;
import com.axiserp.auth.ports.input.GetRoleUseCase;
import com.axiserp.auth.ports.input.ListRolesUseCase;
import com.axiserp.auth.ports.input.UpdateRoleUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleController")
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock private ListRolesUseCase listRolesUseCase;
    @Mock private GetRoleUseCase getRoleUseCase;
    @Mock private CreateRoleUseCase createRoleUseCase;
    @Mock private UpdateRoleUseCase updateRoleUseCase;
    @Mock private DeleteRoleUseCase deleteRoleUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RoleController(
                        listRolesUseCase, getRoleUseCase, createRoleUseCase,
                        updateRoleUseCase, deleteRoleUseCase))
                .build();
    }

    private RoleResponse mockRole() {
        return RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("ADMIN")
                .description("Administrator")
                .build();
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("GET /api/v1/auth/roles - 200 OK")
    void listRoles_success() throws Exception {
        when(listRolesUseCase.listAll()).thenReturn(List.of(mockRole()));
        mockMvc.perform(get("/api/v1/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/roles/{id} - 200 OK")
    void getRole_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(getRoleUseCase.getById(id)).thenReturn(mockRole());
        mockMvc.perform(get("/api/v1/auth/roles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/roles - 201 Created")
    void createRole_success() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("NUEVO_ROL", "Descripcion");
        when(createRoleUseCase.create(any(CreateRoleRequest.class), any(UUID.class)))
                .thenReturn(mockRole());
        mockMvc.perform(post("/api/v1/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/roles - 400 Bad Request")
    void createRole_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"\"}")
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/auth/roles/{id} - 200 OK")
    void updateRole_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest("UPDATED", "Updated desc");
        when(updateRoleUseCase.update(eq(id), any(UpdateRoleRequest.class), any(UUID.class)))
                .thenReturn(mockRole());
        mockMvc.perform(put("/api/v1/auth/roles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/roles/{id} - 200 OK")
    void deleteRole_success() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteRoleUseCase).delete(id);
        mockMvc.perform(delete("/api/v1/auth/roles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/permissions - 200 OK")
    void listPermissions_success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/roles/{roleId}/permissions - 200 OK")
    void getRolePermissions_success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles/{roleId}/permissions", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/roles/{roleId}/permissions - 200 OK")
    void assignPermissions_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/roles/{roleId}/permissions", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/roles/{roleId}/permissions/{permId} - 200 OK")
    void removePermission_success() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/roles/{roleId}/permissions/{permId}",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
