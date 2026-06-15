package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController")
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateCategoryUseCase createCategoryUseCase;
    @Mock private GetCategoryUseCase getCategoryUseCase;
    @Mock private ListCategoriesUseCase listCategoriesUseCase;
    @Mock private UpdateCategoryUseCase updateCategoryUseCase;
    @Mock private DeactivateCategoryUseCase deactivateCategoryUseCase;
    @Mock private ReactivateCategoryUseCase reactivateCategoryUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CategoryController(
                        createCategoryUseCase, getCategoryUseCase, listCategoriesUseCase,
                        updateCategoryUseCase, deactivateCategoryUseCase, reactivateCategoryUseCase))
                .build();
    }

    private CategoryResponse mockCategory() {
        return CategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Category")
                .description("Test description")
                .status("ACTIVO")
                .build();
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("POST /api/v1/categorias - 201 Created")
    void createCategory_success() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("New Category", "Description", null);
        when(createCategoryUseCase.create(any(CreateCategoryRequest.class), any(UUID.class)))
                .thenReturn(mockCategory());
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/categorias - 400 Bad Request")
    void createCategory_invalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}")
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/categorias - 200 OK")
    void listCategories_success() throws Exception {
        when(listCategoriesUseCase.findByFilters(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(mockCategory()));
        when(listCategoriesUseCase.countByFilters(any(), anyBoolean())).thenReturn(1L);
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Test Category"));
    }

    @Test
    @DisplayName("GET /api/v1/categorias - 200 with search")
    void listCategories_withSearch() throws Exception {
        when(listCategoriesUseCase.findByFilters(eq("test"), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(listCategoriesUseCase.countByFilters(eq("test"), anyBoolean())).thenReturn(0L);
        mockMvc.perform(get("/api/v1/categorias")
                        .param("search", "test").param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/categorias/{id} - 200 OK")
    void getCategory_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(getCategoryUseCase.getById(id)).thenReturn(mockCategory());
        mockMvc.perform(get("/api/v1/categorias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Category"));
    }

    @Test
    @DisplayName("PUT /api/v1/categorias/{id} - 200 OK")
    void updateCategory_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated", "Desc", null);
        when(updateCategoryUseCase.update(eq(id), any(UpdateCategoryRequest.class), any(UUID.class)))
                .thenReturn(mockCategory());
        mockMvc.perform(put("/api/v1/categorias/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/categorias/{id}/desactivar - 200 OK")
    void deactivateCategory_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(deactivateCategoryUseCase.deactivate(eq(id), any(UUID.class))).thenReturn(mockCategory());
        mockMvc.perform(patch("/api/v1/categorias/{id}/desactivar", id)
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/categorias/{id}/activar - 200 OK")
    void reactivateCategory_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(reactivateCategoryUseCase.reactivate(eq(id), any(UUID.class))).thenReturn(mockCategory());
        mockMvc.perform(patch("/api/v1/categorias/{id}/activar", id)
                        .with(r -> { r.setUserPrincipal(auth()); return r; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
