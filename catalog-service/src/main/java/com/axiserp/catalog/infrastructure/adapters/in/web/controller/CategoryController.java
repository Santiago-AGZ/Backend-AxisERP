package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.catalog.ports.input.CreateCategoryUseCase;
import com.axiserp.catalog.ports.input.DeactivateCategoryUseCase;
import com.axiserp.catalog.ports.input.GetCategoryUseCase;
import com.axiserp.catalog.ports.input.ListCategoriesUseCase;
import com.axiserp.catalog.ports.input.UpdateCategoryUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            Authentication authentication) {
        UUID createdBy = UUID.fromString((String) authentication.getPrincipal());
        CategoryResponse response = createCategoryUseCase.create(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Categoria creada exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<CategoryResponse> data = listCategoriesUseCase.listAll(page - 1, size);
        long total = listCategoriesUseCase.countAll();
        return ResponseEntity.ok(ApiResponse.paged(
                data, "Categorias recuperadas exitosamente",
                PaginationMeta.of(page, size, total)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getCategoryUseCase.getById(id), "Categoria encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            Authentication authentication) {
        UUID updatedBy = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(updateCategoryUseCase.update(id, request, updatedBy), "Categoria actualizada"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID updatedBy = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(deactivateCategoryUseCase.deactivate(id, updatedBy), "Categoria desactivada"));
    }
}
