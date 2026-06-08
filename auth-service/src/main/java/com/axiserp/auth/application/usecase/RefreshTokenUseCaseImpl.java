package com.axiserp.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.domain.exception.InvalidCredentialsException;
import com.axiserp.auth.ports.input.RefreshTokenUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCaseImpl.class);

    private final SupabaseAuthPort supabaseAuthPort;

    @Override
    @Transactional
    public LoginResponse refresh(String refreshTokenValue, String ipAddress, String userAgent) {
        try {
            var supabaseResponse = supabaseAuthPort.refreshToken(refreshTokenValue);

            log.info("token_refreshed ip={}", ipAddress);

            return LoginResponse.builder()
                    .accessToken(supabaseResponse.accessToken())
                    .refreshToken(supabaseResponse.refreshToken())
                    .role(null)
                    .name(null)
                    .build();

        } catch (Exception e) {
            log.warn("refresh_token_invalid_via_supabase ip={} reason={}", ipAddress, e.getMessage());
            throw new InvalidCredentialsException("Token de refresco inválido o expirado");
        }
    }
}