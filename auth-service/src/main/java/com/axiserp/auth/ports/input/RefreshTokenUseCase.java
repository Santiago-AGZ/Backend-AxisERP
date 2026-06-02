package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.response.LoginResponse;

public interface RefreshTokenUseCase {

    LoginResponse refresh(String refreshToken, String ipAddress, String userAgent);
}
