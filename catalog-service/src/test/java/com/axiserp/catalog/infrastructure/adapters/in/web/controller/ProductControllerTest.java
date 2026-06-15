package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateProductUseCase createProductUseCase;
    @Mock private GetProductUseCase getProductUseCase;
    @Mock private ListProductsUseCase listProductsUseCase;
    @Mock private UpdateProductUseCase updateProductUseCase;
    @Mock private DeactivateProductUseCase deactivateProductUseCase;
    @Mock private ReactivateProductUseCase reactivateProductUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(
                        createProductUseCase, getProductUseCase, listProductsUseCase,
                        updateProductUseCase, deactivateProductUseCase, reactivateProductUseCase))
                .build();
    }

    private ProductResponse mockProduct() {
        return ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .codigo("PROD-001")
                .salePrice(BigDecimal.valueOf(100))
                .purchasePrice(BigDecimal.valueOf(50))
                .status("ACTIVO")
                .build();
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("POST /api/v1/productos - 201 Created")
    void createProduct_success() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "New Product", "PROD-002", "Description",
                UUID.randomUUID(), BigDecimal.valueOf(50), BigDecimal.valueOf(100));
        when(createProductUseCase.create(any(CreateProductRequest.class), any(UUID.class)))
                .thenReturn(mockProduct());
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/productos - 400 Bad Request")
    void createProduct_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"codigo\":\"\",\"categoryId\":\"\",\"purchasePrice\":0,\"salePrice\":0}")
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/productos/{id} - 200 OK")
    void getProduct_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(getProductUseCase.getById(id)).thenReturn(mockProduct());
        mockMvc.perform(get("/api/v1/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/productos - 200 OK")
    void listProducts_success() throws Exception {
        when(listProductsUseCase.list(any(), any(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(mockProduct()), 1L));
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/productos - 200 with search")
    void listProducts_withSearch() throws Exception {
        when(listProductsUseCase.list(eq("test"), any(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/productos")
                        .param("search", "test").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/productos/{id} - 200 OK")
    void updateProduct_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated", "Desc", UUID.randomUUID(),
                BigDecimal.valueOf(60), BigDecimal.valueOf(120));
        when(updateProductUseCase.update(eq(id), any(UpdateProductRequest.class), any(UUID.class)))
                .thenReturn(mockProduct());
        mockMvc.perform(put("/api/v1/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/productos/{id}/desactivar - 200 OK")
    void deactivateProduct_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(deactivateProductUseCase.deactivate(eq(id), any(UUID.class))).thenReturn(mockProduct());
        mockMvc.perform(patch("/api/v1/productos/{id}/desactivar", id)
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/productos/{id}/activar - 200 OK")
    void reactivateProduct_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(reactivateProductUseCase.reactivate(eq(id), any(UUID.class))).thenReturn(mockProduct());
        mockMvc.perform(patch("/api/v1/productos/{id}/activar", id)
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
