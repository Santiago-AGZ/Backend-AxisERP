package com.axiserp.sales.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateCustomerRequest {

    @NotBlank(message = "El código del cliente es obligatorio")
    @Size(max = 20, message = "El código no puede superar 20 caracteres")
    @jakarta.validation.constraints.Pattern(
        regexp = "^CLI-\\d{6}$",
        message = "El código debe tener formato CLI-000001")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
    private String name;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 20, message = "El tipo de documento no puede superar 20 caracteres")
    private String documentType;

    @NotBlank(message = "El numero de documento es obligatorio")
    @Size(max = 50, message = "El numero de documento no puede superar 50 caracteres")
    private String documentNumber;

    @Email(message = "El email no tiene un formato valido")
    @Size(max = 255, message = "El email no puede superar 255 caracteres")
    private String email;

    @Size(max = 50, message = "El telefono no puede superar 50 caracteres")
    private String phone;

    @Size(max = 500, message = "La direccion no puede superar 500 caracteres")
    private String address;
}
