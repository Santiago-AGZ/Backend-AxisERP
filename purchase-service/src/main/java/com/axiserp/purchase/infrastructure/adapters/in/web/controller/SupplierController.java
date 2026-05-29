package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
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

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = createSupplierUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable UUID id) {
        return ResponseEntity.ok(getSupplierUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> listSuppliers() {
        return ResponseEntity.ok(listSuppliersUseCase.execute());
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SupplierResponse> deactivateSupplier(@PathVariable UUID id) {
        return ResponseEntity.ok(deactivateSupplierUseCase.execute(id));
    }
}
