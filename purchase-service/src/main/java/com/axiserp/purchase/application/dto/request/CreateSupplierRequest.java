package com.axiserp.purchase.application.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateSupplierRequest {

    @NotBlank(message = "El código del proveedor es obligatorio")
    @jakarta.validation.constraints.Size(max = 20, message = "El código no puede superar 20 caracteres")
    @jakarta.validation.constraints.Pattern(
        regexp = "^PROV-\\d{6}$",
        message = "El código debe tener formato PROV-000001")
    private String codigo;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    private String name;

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;

    private String phone;
    private String email;
    private String address;
}
