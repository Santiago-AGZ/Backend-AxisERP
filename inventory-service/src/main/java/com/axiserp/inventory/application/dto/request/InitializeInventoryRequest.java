package com.axiserp.inventory.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
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
public class InitializeInventoryRequest {

    @NotNull(message = "El productId es obligatorio")
    private UUID productId;

    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    private int initialStock;

    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    private int minStock;

    @Min(value = 0, message = "El stock maximo no puede ser negativo")
    private int maxStock;

    private String notes;
}
