package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.catalog.application.dto.request.CreateProductRequest;
import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.shared.PageResult;
import com.axiserp.catalog.ports.input.CreateProductUseCase;
import com.axiserp.catalog.ports.input.DeactivateProductUseCase;
import com.axiserp.catalog.ports.input.GetProductUseCase;
import com.axiserp.catalog.ports.input.ListProductsUseCase;
import com.axiserp.catalog.ports.input.ReactivateProductUseCase;
import com.axiserp.catalog.ports.input.UpdateProductUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ProductController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ProductController Security")
class ProductControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateProductUseCase createProductUseCase;
    @MockitoBean private GetProductUseCase getProductUseCase;
    @MockitoBean private ListProductsUseCase listProductsUseCase;
    @MockitoBean private UpdateProductUseCase updateProductUseCase;
    @MockitoBean private DeactivateProductUseCase deactivateProductUseCase;
    @MockitoBean private ReactivateProductUseCase reactivateProductUseCase;

    private static final UUID PID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private ProductResponse mockProduct() {
        return ProductResponse.builder().id(PID).name("Test").codigo("P001")
                .salePrice(BigDecimal.TEN).purchasePrice(BigDecimal.ONE).status("ACTIVO").build();
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/productos ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createProductUseCase.create(any(), any())).thenReturn(mockProduct());
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateProductRequest("N","C","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/productos INVENTARIO 201")
    void create_asInventario() throws Exception {
        when(createProductUseCase.create(any(), any())).thenReturn(mockProduct());
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateProductRequest("N","C","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/productos VENDEDOR 403")
    void create_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateProductRequest("N","C","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/productos UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateProductRequest("N","C","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/productos/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getProductUseCase.getById(any())).thenReturn(mockProduct());
        mockMvc.perform(get("/api/v1/productos/{id}", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/productos/{id} VENDEDOR 200")
    void get_asVendedor() throws Exception {
        when(getProductUseCase.getById(any())).thenReturn(mockProduct());
        mockMvc.perform(get("/api/v1/productos/{id}", PID)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} UNAUTH 401")
    void get_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/productos/{id}", PID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/productos ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listProductsUseCase.list(any(), any(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(mockProduct()), 1L));
        mockMvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/productos VENDEDOR 200")
    void list_asVendedor() throws Exception {
        when(listProductsUseCase.list(any(), any(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/productos UNAUTH 401")
    void list_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/productos")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PUT /api/v1/productos/{id} ADMIN 200")
    void update_asAdmin() throws Exception {
        when(updateProductUseCase.update(any(), any(), any())).thenReturn(mockProduct());
        mockMvc.perform(put("/api/v1/productos/{id}", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProductRequest("U","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PUT /api/v1/productos/{id} INVENTARIO 200")
    void update_asInventario() throws Exception {
        when(updateProductUseCase.update(any(), any(), any())).thenReturn(mockProduct());
        mockMvc.perform(put("/api/v1/productos/{id}", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProductRequest("U","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PUT /api/v1/productos/{id} VENDEDOR 403")
    void update_asVendedor() throws Exception {
        mockMvc.perform(put("/api/v1/productos/{id}", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProductRequest("U","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{id} UNAUTH 401")
    void update_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/productos/{id}", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateProductRequest("U","D",UUID.randomUUID(),BigDecimal.ONE,BigDecimal.TEN))))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/productos/{id}/desactivar ADMIN 200")
    void deactivate_asAdmin() throws Exception {
        when(deactivateProductUseCase.deactivate(any(), any())).thenReturn(mockProduct());
        mockMvc.perform(patch("/api/v1/productos/{id}/desactivar", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/productos/{id}/desactivar INVENTARIO 403")
    void deactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/{id}/desactivar", PID)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/productos/{id}/desactivar UNAUTH 401")
    void deactivate_unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/{id}/desactivar", PID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/productos/{id}/activar ADMIN 200")
    void reactivate_asAdmin() throws Exception {
        when(reactivateProductUseCase.reactivate(any(), any())).thenReturn(mockProduct());
        mockMvc.perform(patch("/api/v1/productos/{id}/activar", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/productos/{id}/activar INVENTARIO 403")
    void reactivate_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/{id}/activar", PID)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/productos/{id}/activar UNAUTH 401")
    void reactivate_unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/productos/{id}/activar", PID)).andExpect(status().isUnauthorized());
    }
}
