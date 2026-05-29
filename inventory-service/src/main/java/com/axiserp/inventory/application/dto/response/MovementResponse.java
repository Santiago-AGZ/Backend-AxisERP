package com.axiserp.inventory.application.dto.response;

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
public class MovementResponse {

    private UUID id;
    private UUID inventoryId;
    private UUID productId;
    private String movementType;
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
