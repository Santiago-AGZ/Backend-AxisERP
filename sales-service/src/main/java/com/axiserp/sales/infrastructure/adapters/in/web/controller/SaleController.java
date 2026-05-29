package com.axiserp.sales.infrastructure.adapters.in.web.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.ports.input.ConfirmSaleUseCase;
import com.axiserp.sales.ports.input.CreateSaleUseCase;
import com.axiserp.sales.ports.input.GetSaleUseCase;
import com.axiserp.sales.ports.input.ListSalesUseCase;
import com.axiserp.sales.ports.input.PaySaleUseCase;
import com.axiserp.sales.ports.input.VoidSaleUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final CreateSaleUseCase createSaleUseCase;
    private final GetSaleUseCase getSaleUseCase;
    private final ListSalesUseCase listSalesUseCase;
    private final ConfirmSaleUseCase confirmSaleUseCase;
    private final PaySaleUseCase paySaleUseCase;
    private final VoidSaleUseCase voidSaleUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PostMapping
    public ResponseEntity<SaleResponse> createSale(
            @Valid @RequestBody CreateSaleRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        SaleResponse response = createSaleUseCase.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable UUID id) {
        return ResponseEntity.ok(getSaleUseCase.getById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<List<SaleResponse>> listSales(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(listSalesUseCase.list(customerId, status, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<SaleResponse> confirmSale(@PathVariable UUID id) {
        return ResponseEntity.ok(confirmSaleUseCase.confirm(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PatchMapping("/{id}/pay")
    public ResponseEntity<SaleResponse> paySale(@PathVariable UUID id) {
        return ResponseEntity.ok(paySaleUseCase.pay(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/void")
    public ResponseEntity<SaleResponse> voidSale(@PathVariable UUID id) {
        return ResponseEntity.ok(voidSaleUseCase.voidSale(id));
    }
}
