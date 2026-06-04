package com.axiserp.inventory.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.inventory.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.inventory.ports.input.GetInventoryUseCase;
import com.axiserp.inventory.ports.input.GetDepletedAlertsUseCase;
import com.axiserp.inventory.ports.input.GetLowStockAlertsUseCase;
import com.axiserp.inventory.ports.input.InitializeInventoryUseCase;
import com.axiserp.inventory.ports.input.ListMovementsUseCase;
import com.axiserp.inventory.ports.input.ListProductsUseCase;
import com.axiserp.inventory.ports.input.RegisterAdjustmentUseCase;
import com.axiserp.inventory.ports.input.RegisterEntryUseCase;
import com.axiserp.inventory.ports.input.RegisterExitUseCase;
import com.axiserp.inventory.ports.input.RegisterReturnUseCase;
import com.axiserp.inventory.ports.input.ReverseMovementUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InitializeInventoryUseCase initializeInventoryUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetLowStockAlertsUseCase getLowStockAlertsUseCase;
    private final GetDepletedAlertsUseCase getDepletedAlertsUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final ListMovementsUseCase listMovementsUseCase;
    private final RegisterEntryUseCase registerEntryUseCase;
    private final RegisterExitUseCase registerExitUseCase;
    private final RegisterReturnUseCase registerReturnUseCase;
    private final RegisterAdjustmentUseCase registerAdjustmentUseCase;
    private final ReverseMovementUseCase reverseMovementUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<InventoryResponse>> initialize(
            @Valid @RequestBody InitializeInventoryRequest request) {
        InventoryResponse response = initializeInventoryUseCase.initialize(request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Inventario inicializado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductInventoryResponse>>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID categoryId) {
        var result = listProductsUseCase.list(page - 1, size, categoryId);
        return ResponseEntity.ok(ApiResponse.paged(
                result.getContent(), "Productos listados",
                PaginationMeta.of(page, size, result.getTotal())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<ProductInventoryResponse>>> getLowStockAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = getLowStockAlertsUseCase.execute(page - 1, size);
        return ResponseEntity.ok(ApiResponse.paged(
                result.getContent(), "Alertas de stock minimo",
                PaginationMeta.of(page, size, result.getTotal())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/alerts/depleted")
    public ResponseEntity<ApiResponse<List<ProductInventoryResponse>>> getDepletedAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = getDepletedAlertsUseCase.execute(page - 1, size);
        return ResponseEntity.ok(ApiResponse.paged(
                result.getContent(), "Alertas de agotamiento",
                PaginationMeta.of(page, size, result.getTotal())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO', 'VENDEDOR')")
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(
                getInventoryUseCase.getByProductId(productId), "Inventario encontrado"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<ApiResponse<List<MovementResponse>>> listMovements(@PathVariable UUID productId) {
        List<MovementResponse> data = listMovementsUseCase.listByProductId(productId);
        return ResponseEntity.ok(ApiResponse.paged(
                data, "Movimientos recuperados exitosamente",
                PaginationMeta.of(1, data.size(), data.size())));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/products/{productId}/entry")
    public ResponseEntity<ApiResponse<MovementResponse>> registerEntry(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        MovementResponse response = registerEntryUseCase.registerEntry(
                productId, quantity, referenceType, referenceId, notes, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Entrada registrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/products/{productId}/exit")
    public ResponseEntity<ApiResponse<MovementResponse>> registerExit(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        MovementResponse response = registerExitUseCase.registerExit(
                productId, quantity, referenceType, referenceId, notes, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Salida registrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/products/{productId}/return")
    public ResponseEntity<ApiResponse<MovementResponse>> registerReturn(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        MovementResponse response = registerReturnUseCase.registerReturn(
                productId, quantity, referenceType, referenceId, notes, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Devolución registrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTARIO')")
    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<ApiResponse<MovementResponse>> registerAdjustment(
            @PathVariable UUID productId,
            @Valid @RequestBody AdjustmentRequest request) {
        MovementResponse response = registerAdjustmentUseCase.registerAdjustment(
                productId, request, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Ajuste registrado"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/movements/{movementId}/reverse")
    public ResponseEntity<ApiResponse<MovementResponse>> reverseMovement(
            @PathVariable UUID movementId,
            @RequestParam String justification) {
        MovementResponse response = reverseMovementUseCase.reverse(
                movementId, justification, currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Movimiento revertido"));
    }

    private UUID currentUserId() {
        return UUID.fromString(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
