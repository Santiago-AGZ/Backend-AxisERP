package com.axiserp.inventory.infrastructure.adapters.in.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.axiserp.inventory.ports.input.GetInventoryUseCase;
import com.axiserp.inventory.ports.input.InitializeInventoryUseCase;
import com.axiserp.inventory.ports.input.ListMovementsUseCase;
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
    private final GetInventoryUseCase getInventoryUseCase;
    private final ListMovementsUseCase listMovementsUseCase;
    private final RegisterEntryUseCase registerEntryUseCase;
    private final RegisterExitUseCase registerExitUseCase;
    private final RegisterReturnUseCase registerReturnUseCase;
    private final RegisterAdjustmentUseCase registerAdjustmentUseCase;
    private final ReverseMovementUseCase reverseMovementUseCase;

    @PostMapping("/initialize")
    public ResponseEntity<InventoryResponse> initialize(@Valid @RequestBody InitializeInventoryRequest request) {
        UUID userId = currentUserId();
        InventoryResponse response = initializeInventoryUseCase.initialize(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(getInventoryUseCase.getByProductId(productId));
    }

    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<List<MovementResponse>> listMovements(@PathVariable UUID productId) {
        return ResponseEntity.ok(listMovementsUseCase.listByProductId(productId));
    }

    @PostMapping("/products/{productId}/entry")
    public ResponseEntity<MovementResponse> registerEntry(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        UUID userId = currentUserId();
        MovementResponse response = registerEntryUseCase.registerEntry(productId, quantity, referenceType, referenceId, notes, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/products/{productId}/exit")
    public ResponseEntity<MovementResponse> registerExit(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        UUID userId = currentUserId();
        MovementResponse response = registerExitUseCase.registerExit(productId, quantity, referenceType, referenceId, notes, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/products/{productId}/return")
    public ResponseEntity<MovementResponse> registerReturn(
            @PathVariable UUID productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) String notes) {
        UUID userId = currentUserId();
        MovementResponse response = registerReturnUseCase.registerReturn(productId, quantity, referenceType, referenceId, notes, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<MovementResponse> registerAdjustment(
            @PathVariable UUID productId,
            @Valid @RequestBody AdjustmentRequest request) {
        UUID userId = currentUserId();
        MovementResponse response = registerAdjustmentUseCase.registerAdjustment(productId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/movements/{movementId}/reverse")
    public ResponseEntity<MovementResponse> reverseMovement(
            @PathVariable UUID movementId,
            @RequestParam String justification) {
        UUID userId = currentUserId();
        MovementResponse response = reverseMovementUseCase.reverse(movementId, justification, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
