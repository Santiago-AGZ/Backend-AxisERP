package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.purchase.ports.input.CancelPurchaseUseCase;
import com.axiserp.purchase.ports.input.CreatePurchaseUseCase;
import com.axiserp.purchase.ports.input.GetPurchaseUseCase;
import com.axiserp.purchase.ports.input.ListPurchasesUseCase;
import com.axiserp.purchase.ports.input.ReceivePurchaseUseCase;
import com.axiserp.purchase.ports.input.UpdatePurchaseStatusUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final CreatePurchaseUseCase createPurchaseUseCase;
    private final GetPurchaseUseCase getPurchaseUseCase;
    private final ListPurchasesUseCase listPurchasesUseCase;
    private final UpdatePurchaseStatusUseCase updatePurchaseStatusUseCase;
    private final ReceivePurchaseUseCase receivePurchaseUseCase;
    private final CancelPurchaseUseCase cancelPurchaseUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {
        UUID userId = UUID.fromString(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        PurchaseResponse response = createPurchaseUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Compra creada exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchase(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getPurchaseUseCase.execute(id), "Compra encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> listPurchases(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<PurchaseResponse> data;
        long total;
        if ((search != null && !search.isBlank()) || (status != null && !status.isBlank())) {
            data = listPurchasesUseCase.execute(search, status, page - 1, size);
            total = listPurchasesUseCase.countByFilters(search, status);
        } else {
            data = listPurchasesUseCase.execute();
            total = listPurchasesUseCase.countAll();
        }
        return ResponseEntity.ok(ApiResponse.paged(
                data, "Compras recuperadas exitosamente",
                PaginationMeta.of(page, size, total)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam PurchaseStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(
                updatePurchaseStatusUseCase.execute(id, status), "Estado actualizado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseResponse>> receive(
            @PathVariable UUID id,
            @Valid @RequestBody ReceivePurchaseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                receivePurchaseUseCase.execute(id, request), "Compra recibida"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(cancelPurchaseUseCase.execute(id), "Compra cancelada"));
    }
}
