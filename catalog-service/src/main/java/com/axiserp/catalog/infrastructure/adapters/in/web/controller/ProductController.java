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

import com.axiserp.catalog.application.dto.request.CreateProductRequest;
import com.axiserp.catalog.application.dto.request.UpdateProductRequest;
import com.axiserp.catalog.application.dto.response.ProductResponse;
import com.axiserp.catalog.application.shared.PageResult;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.catalog.ports.input.CreateProductUseCase;
import com.axiserp.catalog.ports.input.DeactivateProductUseCase;
import com.axiserp.catalog.ports.input.ReactivateProductUseCase;
import com.axiserp.catalog.ports.input.GetProductUseCase;
import com.axiserp.catalog.ports.input.ListProductsUseCase;
import com.axiserp.catalog.ports.input.UpdateProductUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final ReactivateProductUseCase reactivateProductUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        ProductResponse response = createProductUseCase.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Producto creado exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getProductUseCase.getById(id), "Producto encontrado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResult<ProductResponse> result = listProductsUseCase.list(search, codigo, categoryId, includeInactive, page - 1, size);
        return ResponseEntity.ok(ApiResponse.paged(
                result.content(), "Productos recuperados exitosamente",
                PaginationMeta.of(page, size, result.totalElements())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(updateProductUseCase.update(id, request, userId), "Producto actualizado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(deactivateProductUseCase.deactivate(id, userId), "Producto desactivado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<ProductResponse>> reactivateProduct(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(ApiResponse.ok(reactivateProductUseCase.reactivate(id, userId), "Producto activado"));
    }
}
