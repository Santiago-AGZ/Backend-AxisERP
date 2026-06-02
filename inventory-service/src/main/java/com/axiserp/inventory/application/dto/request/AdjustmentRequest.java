package com.axiserp.inventory.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AdjustmentRequest {

    @NotNull(message = "El tipo de ajuste es obligatorio (POSITIVO o NEGATIVO)")
    private AdjustmentType adjustmentType;

    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private int quantity;

    @NotBlank(message = "La justificacion es obligatoria para ajustes")
    private String justification;

    private String notes;

    public enum AdjustmentType {
        POSITIVO, NEGATIVO
    }
}
