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
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
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
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        SaleResponse response = createSaleUseCase.create(request, userId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Venta creada exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getSaleUseCase.getById(id), "Venta encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleResponse>>> listSales(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = listSalesUseCase.list(customerId, status, productId, page - 1, size);
        return ResponseEntity.ok(ApiResponse.paged(
                result.getContent(), "Ventas recuperadas exitosamente",
                PaginationMeta.of(page, size, result.getTotalRecords())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SaleResponse>> confirmSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(confirmSaleUseCase.confirm(id), "Venta confirmada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PatchMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<SaleResponse>> paySale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(paySaleUseCase.pay(id), "Venta pagada"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/void")
    public ResponseEntity<ApiResponse<SaleResponse>> voidSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(voidSaleUseCase.voidSale(id), "Venta anulada"));
    }
}
