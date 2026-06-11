package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordConfirmationRequest(
    @NotBlank(message = "La contraseña actual es obligatoria")
    String currentPassword
) {}
