package com.axiserp.sales.application.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateSaleRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private UUID customerId;

    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    private List<SaleItemRequest> items;

    private String notes;
}
