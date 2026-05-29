package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {
        UUID userId = UUID.fromString(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        PurchaseResponse response = createPurchaseUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable UUID id) {
        return ResponseEntity.ok(getPurchaseUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseResponse>> listPurchases() {
        return ResponseEntity.ok(listPurchasesUseCase.execute());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam PurchaseStatus status) {
        return ResponseEntity.ok(updatePurchaseStatusUseCase.execute(id, status));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponse> receive(
            @PathVariable UUID id,
            @Valid @RequestBody ReceivePurchaseRequest request) {
        return ResponseEntity.ok(receivePurchaseUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(cancelPurchaseUseCase.execute(id));
    }
}
