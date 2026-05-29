package com.axiserp.sales.infrastructure.adapters.in.web.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.sales.application.dto.response.InvoiceResponse;
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.sales.ports.input.GetInvoiceUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final GetInvoiceUseCase getInvoiceUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getInvoiceUseCase.getById(id), "Factura encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/by-sale/{saleId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceBySale(@PathVariable UUID saleId) {
        return ResponseEntity.ok(ApiResponse.ok(getInvoiceUseCase.getBySaleId(saleId), "Factura encontrada"));
    }
}
