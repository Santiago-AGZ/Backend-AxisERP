package com.axiserp.sales.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
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
public class SaleItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productId;

    @NotNull(message = "El nombre del producto es obligatorio")
    private String productName;

    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private int quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor que cero")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
    private BigDecimal discount = BigDecimal.ZERO;
}
