package com.axiserp.auth.ports.input;

import com.axiserp.auth.application.dto.request.PasswordResetRequest;

public interface ResetPasswordUseCase {

    void resetPassword(String token, String newPassword);
}
