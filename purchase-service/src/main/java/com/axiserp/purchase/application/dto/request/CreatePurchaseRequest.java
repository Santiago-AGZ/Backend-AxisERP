package com.axiserp.purchase.application.dto.request;

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
public class CreatePurchaseRequest {

    @NotNull(message = "El ID del proveedor es obligatorio")
    private UUID supplierId;

    @NotEmpty(message = "La compra debe tener al menos un producto")
    @Valid
    private List<PurchaseItemRequest> items;

    private String notes;
}
