package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.purchase.ports.input.CreateSupplierUseCase;
import com.axiserp.purchase.ports.input.DeactivateSupplierUseCase;
import com.axiserp.purchase.ports.input.GetSupplierUseCase;
import com.axiserp.purchase.ports.input.ListSuppliersUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final CreateSupplierUseCase createSupplierUseCase;
    private final GetSupplierUseCase getSupplierUseCase;
    private final ListSuppliersUseCase listSuppliersUseCase;
    private final DeactivateSupplierUseCase deactivateSupplierUseCase;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = createSupplierUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Proveedor creado exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{codigo}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable String codigo) {
        SupplierResponse response = getSupplierUseCase.executeByCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.ok(response, "Proveedor encontrado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> listSuppliers() {
        List<SupplierResponse> data = listSuppliersUseCase.execute();
        return ResponseEntity.ok(ApiResponse.paged(
                data, "Proveedores recuperados exitosamente",
                PaginationMeta.of(1, data.size(), data.size())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<SupplierResponse>> deactivateSupplier(@PathVariable UUID id) {
        SupplierResponse response = deactivateSupplierUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Proveedor desactivado"));
    }
}
