package com.axiserp.catalog.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "El codigo del producto es obligatorio")
    private String codigo;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "La categoria es obligatoria")
    private UUID categoryId;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    private BigDecimal purchasePrice;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal salePrice;
}
