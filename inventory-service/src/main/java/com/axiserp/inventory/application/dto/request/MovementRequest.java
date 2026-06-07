package com.axiserp.inventory.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
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
public class MovementRequest {

    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private int quantity;

    private String referenceType;

    private UUID referenceId;

    private String notes;
}
