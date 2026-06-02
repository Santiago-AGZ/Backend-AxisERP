package com.axiserp.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
    @NotBlank(message = "El código OTP es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    String otpCode
) {}
