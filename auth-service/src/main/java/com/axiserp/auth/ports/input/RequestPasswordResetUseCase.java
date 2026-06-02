package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.request.PasswordResetRequest;

public interface RequestPasswordResetUseCase {

    void requestReset(String email);
}
