package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}
