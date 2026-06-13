package com.axiserp.auth.ports.output;

import java.util.UUID;

public interface SupabaseAuthPort {
    SupabaseUser createUser(String email, String roleName, String name, UUID createdBy);
    void sendPasswordReset(String email);
    void resetPassword(String recoveryToken, String newPassword);
    RefreshTokenResponse refreshToken(String refreshToken);
    LoginResponse login(String email, String password);

    record RefreshTokenResponse(String accessToken, String refreshToken, int expiresIn) {}

    record LoginResponse(String accessToken, String refreshToken, int expiresIn, String tokenType) {}
}
