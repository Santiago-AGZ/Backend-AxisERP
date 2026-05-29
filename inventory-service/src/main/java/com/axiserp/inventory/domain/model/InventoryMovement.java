package com.axiserp.inventory.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    private UUID id;
    private UUID inventoryId;
    private UUID productId;
    private MovementType movementType;
    private int quantity;
    private int previousStock;
    private int newStock;
    private String referenceType;
    private UUID referenceId;
    private String justification;
    private String notes;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
