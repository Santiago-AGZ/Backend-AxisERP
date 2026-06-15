package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

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

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.request.UpdateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.input.CreateSupplierUseCase;
import com.axiserp.purchase.ports.input.DeactivateSupplierUseCase;
import com.axiserp.purchase.ports.input.GetSupplierUseCase;
import com.axiserp.purchase.ports.input.ListSuppliersUseCase;
import com.axiserp.purchase.ports.input.ReactivateSupplierUseCase;
import com.axiserp.purchase.ports.input.UpdateSupplierUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = SupplierController.class)
@Import(TestSecurityConfig.class)
@DisplayName("SupplierController Security")
class SupplierControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateSupplierUseCase createSupplierUseCase;
    @MockitoBean private GetSupplierUseCase getSupplierUseCase;
    @MockitoBean private ListSuppliersUseCase listSuppliersUseCase;
    @MockitoBean private DeactivateSupplierUseCase deactivateSupplierUseCase;
    @MockitoBean private ReactivateSupplierUseCase reactivateSupplierUseCase;
    @MockitoBean private UpdateSupplierUseCase updateSupplierUseCase;

    private static final UUID SID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private SupplierResponse mockSup() {
        return SupplierResponse.builder().id(SID).codigo("P001").name("Test").nit("123").status(SupplierStatus.ACTIVO).build();
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/suppliers ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createSupplierUseCase.execute(any())).thenReturn(mockSup());
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSupplierRequest.builder().codigo("PROV-000001").name("N").nit("123").build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/suppliers VENDEDOR 403")
    void create_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSupplierRequest.builder().codigo("PROV-000001").name("N").nit("123").build())))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/suppliers UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/suppliers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSupplierRequest.builder().codigo("PROV-000001").name("N").nit("123").build())))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/suppliers/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getSupplierUseCase.execute(any())).thenReturn(mockSup());
        mockMvc.perform(get("/api/v1/suppliers/{id}", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/suppliers/{id} VENDEDOR 200")
    void get_asVendedor() throws Exception {
        when(getSupplierUseCase.execute(any())).thenReturn(mockSup());
        mockMvc.perform(get("/api/v1/suppliers/{id}", SID)).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/suppliers/{id} UNAUTH 401")
    void get_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers/{id}", SID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/suppliers ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listSuppliersUseCase.execute()).thenReturn(List.of());
        when(listSuppliersUseCase.countAll()).thenReturn(0L);
        mockMvc.perform(get("/api/v1/suppliers")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/suppliers VENDEDOR 200")
    void list_asVendedor() throws Exception {
        when(listSuppliersUseCase.execute()).thenReturn(List.of());
        when(listSuppliersUseCase.countAll()).thenReturn(0L);
        mockMvc.perform(get("/api/v1/suppliers")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/suppliers UNAUTH 401")
    void list_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PUT /api/v1/suppliers/{id} ADMIN 200")
    void update_asAdmin() throws Exception {
        when(updateSupplierUseCase.execute(any(), any())).thenReturn(mockSup());
        mockMvc.perform(put("/api/v1/suppliers/{id}", SID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateSupplierRequest.builder().name("U").build())))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PUT /api/v1/suppliers/{id} INVENTARIO 403")
    void update_asInventario() throws Exception {
        mockMvc.perform(put("/api/v1/suppliers/{id}", SID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateSupplierRequest.builder().name("U").build())))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("PUT /api/v1/suppliers/{id} UNAUTH 401")
    void update_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/suppliers/{id}", SID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(UpdateSupplierRequest.builder().name("U").build())))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/suppliers/{id}/deactivate ADMIN 200")
    void deactivate_asAdmin() throws Exception {
        when(deactivateSupplierUseCase.execute(any())).thenReturn(mockSup());
        mockMvc.perform(patch("/api/v1/suppliers/{id}/deactivate", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/suppliers/{id}/deactivate INVENTARIO 403")
    void deactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/suppliers/{id}/deactivate", SID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/suppliers/{id}/reactivate ADMIN 200")
    void reactivate_asAdmin() throws Exception {
        when(reactivateSupplierUseCase.execute(any())).thenReturn(mockSup());
        mockMvc.perform(patch("/api/v1/suppliers/{id}/reactivate", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/suppliers/{id}/reactivate INVENTARIO 403")
    void reactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/suppliers/{id}/reactivate", SID)).andExpect(status().isForbidden());
    }
}
