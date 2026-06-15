package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

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

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.ports.input.CreateCategoryUseCase;
import com.axiserp.catalog.ports.input.DeactivateCategoryUseCase;
import com.axiserp.catalog.ports.input.GetCategoryUseCase;
import com.axiserp.catalog.ports.input.ListCategoriesUseCase;
import com.axiserp.catalog.ports.input.ReactivateCategoryUseCase;
import com.axiserp.catalog.ports.input.UpdateCategoryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CategoryController.class)
@Import(TestSecurityConfig.class)
@DisplayName("CategoryController Security")
class CategoryControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateCategoryUseCase createCategoryUseCase;
    @MockitoBean private GetCategoryUseCase getCategoryUseCase;
    @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean private UpdateCategoryUseCase updateCategoryUseCase;
    @MockitoBean private DeactivateCategoryUseCase deactivateCategoryUseCase;
    @MockitoBean private ReactivateCategoryUseCase reactivateCategoryUseCase;

    private static final UUID CID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private CategoryResponse mockCat() {
        return CategoryResponse.builder().id(CID).name("Test").description("D").status("ACTIVO").build();
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/categorias ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createCategoryUseCase.create(any(), any())).thenReturn(mockCat());
        mockMvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCategoryRequest("N","D",null))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/categorias INVENTARIO 201")
    void create_asInventario() throws Exception {
        when(createCategoryUseCase.create(any(), any())).thenReturn(mockCat());
        mockMvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCategoryRequest("N","D",null))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/categorias VENDEDOR 403")
    void create_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCategoryRequest("N","D",null))))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/categorias UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCategoryRequest("N","D",null))))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/categorias ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listCategoriesUseCase.findByFilters(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(List.of(mockCat()));
        when(listCategoriesUseCase.countByFilters(any(), anyBoolean())).thenReturn(1L);
        mockMvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/categorias VENDEDOR 200")
    void list_asVendedor() throws Exception {
        when(listCategoriesUseCase.findByFilters(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(List.of());
        when(listCategoriesUseCase.countByFilters(any(), anyBoolean())).thenReturn(0L);
        mockMvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/categorias UNAUTH 401")
    void list_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/categorias")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/categorias/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getCategoryUseCase.getById(any())).thenReturn(mockCat());
        mockMvc.perform(get("/api/v1/categorias/{id}", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/categorias/{id} VENDEDOR 200")
    void get_asVendedor() throws Exception {
        when(getCategoryUseCase.getById(any())).thenReturn(mockCat());
        mockMvc.perform(get("/api/v1/categorias/{id}", CID)).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/categorias/{id} UNAUTH 401")
    void get_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/categorias/{id}", CID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PUT /api/v1/categorias/{id} ADMIN 200")
    void update_asAdmin() throws Exception {
        when(updateCategoryUseCase.update(any(), any(), any())).thenReturn(mockCat());
        mockMvc.perform(put("/api/v1/categorias/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCategoryRequest("U","D",null))))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PUT /api/v1/categorias/{id} INVENTARIO 200")
    void update_asInventario() throws Exception {
        when(updateCategoryUseCase.update(any(), any(), any())).thenReturn(mockCat());
        mockMvc.perform(put("/api/v1/categorias/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCategoryRequest("U","D",null))))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PUT /api/v1/categorias/{id} VENDEDOR 403")
    void update_asVendedor() throws Exception {
        mockMvc.perform(put("/api/v1/categorias/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCategoryRequest("U","D",null))))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("PUT /api/v1/categorias/{id} UNAUTH 401")
    void update_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/categorias/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCategoryRequest("U","D",null))))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/categorias/{id}/desactivar ADMIN 200")
    void deactivate_asAdmin() throws Exception {
        when(deactivateCategoryUseCase.deactivate(any(), any())).thenReturn(mockCat());
        mockMvc.perform(patch("/api/v1/categorias/{id}/desactivar", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/categorias/{id}/desactivar INVENTARIO 403")
    void deactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/categorias/{id}/desactivar", CID)).andExpect(status().isForbidden());
    }

    @Test @DisplayName("PATCH /api/v1/categorias/{id}/desactivar UNAUTH 401")
    void deactivate_unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/categorias/{id}/desactivar", CID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/categorias/{id}/activar ADMIN 200")
    void reactivate_asAdmin() throws Exception {
        when(reactivateCategoryUseCase.reactivate(any(), any())).thenReturn(mockCat());
        mockMvc.perform(patch("/api/v1/categorias/{id}/activar", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/categorias/{id}/activar INVENTARIO 403")
    void reactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/categorias/{id}/activar", CID)).andExpect(status().isForbidden());
    }

    @Test @DisplayName("PATCH /api/v1/categorias/{id}/activar UNAUTH 401")
    void reactivate_unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/categorias/{id}/activar", CID)).andExpect(status().isUnauthorized());
    }
}
