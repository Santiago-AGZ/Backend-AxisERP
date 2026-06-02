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

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.sales.ports.input.CreateCustomerUseCase;
import com.axiserp.sales.ports.input.DeactivateCustomerUseCase;
import com.axiserp.sales.ports.input.GetCustomerUseCase;
import com.axiserp.sales.ports.input.ListCustomersUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CustomerResponse response = createCustomerUseCase.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Cliente creado exitosamente"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{codigo}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable String codigo) {
        CustomerResponse response = getCustomerUseCase.getByCodigo(codigo);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cliente encontrado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> listCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<CustomerResponse> data = listCustomersUseCase.list(search, includeInactive, page - 1, size);
        return ResponseEntity.ok(ApiResponse.paged(
                data,
                "Clientes recuperados exitosamente",
                PaginationMeta.of(page, size, data.size())));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CustomerResponse>> deactivateCustomer(@PathVariable UUID id) {
        CustomerResponse response = deactivateCustomerUseCase.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cliente desactivado"));
    }
}
