package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank(message = "El nombre del rol es obligatorio")
    private String name;

    @NotBlank(message = "La descripcion del rol es obligatoria")
    private String description;
}
