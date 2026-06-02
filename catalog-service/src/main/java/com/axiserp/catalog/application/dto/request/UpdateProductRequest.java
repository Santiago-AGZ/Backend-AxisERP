package com.axiserp.catalog.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;

    private UUID categoryId;

    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal salePrice;
}
