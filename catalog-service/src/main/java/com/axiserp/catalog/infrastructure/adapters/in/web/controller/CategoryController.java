package com.axiserp.catalog.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.catalog.application.dto.request.CreateCategoryRequest;
import com.axiserp.catalog.application.dto.request.UpdateCategoryRequest;
import com.axiserp.catalog.application.dto.response.CategoryResponse;
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
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        CategoryResponse response = createCategoryUseCase.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories() {
        return ResponseEntity.ok(listCategoriesUseCase.listAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(getCategoryUseCase.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        return ResponseEntity.ok(updateCategoryUseCase.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<CategoryResponse> deactivateCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(deactivateCategoryUseCase.deactivate(id));
    }
}
