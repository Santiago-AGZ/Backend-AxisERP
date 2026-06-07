package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "formato de correo inválido")
    private String email;

    @NotBlank(message = "El rol es obligatorio")
    private String role;
}
