package com.axiserp.auth.ports.input;

public interface LogoutUseCase {

    void logout(String refreshToken);
}
