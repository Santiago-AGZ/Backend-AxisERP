package com.axiserp.sales.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class UpdateCustomerRequest {

    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String name;

    @Email(message = "El email no tiene un formato valido")
    @Size(max = 255, message = "El email no puede superar 255 caracteres")
    private String email;

    @Size(max = 50, message = "El telefono no puede superar 50 caracteres")
    private String phone;

    @Size(max = 500, message = "La direccion no puede superar 500 caracteres")
    private String address;
}
