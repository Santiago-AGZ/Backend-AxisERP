package com.axiserp.purchase.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
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
public class PurchaseItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private int quantity;

    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor que cero")
    private BigDecimal unitPrice;
}
