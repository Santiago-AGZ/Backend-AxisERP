package com.axiserp.purchase.application.dto.request;

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
public class ReceiveItemRequest {

    @NotNull(message = "El ID del item es obligatorio")
    private UUID itemId;

    @Min(value = 1, message = "La cantidad recibida debe ser mayor que cero")
    private int receivedQuantity;
}
