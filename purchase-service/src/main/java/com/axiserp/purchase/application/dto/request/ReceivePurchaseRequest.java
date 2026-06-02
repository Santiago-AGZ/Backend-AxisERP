package com.axiserp.purchase.application.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class ReceivePurchaseRequest {

    @NotEmpty(message = "Debe especificar al menos un item a recibir")
    @Valid
    private List<ReceiveItemRequest> items;
}
