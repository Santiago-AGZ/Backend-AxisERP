package com.axiserp.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.domain.exception.InvalidCredentialsException;
import com.axiserp.auth.ports.input.RefreshTokenUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCaseImpl.class);

    private final SupabaseAuthPort supabaseAuthPort;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponse refresh(String refreshTokenValue, String ipAddress, String userAgent) {
        try {
            var storedToken = refreshTokenService.validateByToken(refreshTokenValue);

            var supabaseResponse = supabaseAuthPort.refreshToken(refreshTokenValue);

            refreshTokenService.revoke(refreshTokenValue);
            refreshTokenService.saveExternalToken(storedToken.getUserId(), supabaseResponse.refreshToken(), ipAddress, userAgent);

            log.info("token_refreshed user_id={} ip={}", storedToken.getUserId(), ipAddress);

            return LoginResponse.builder()
                    .accessToken(supabaseResponse.accessToken())
                    .refreshToken(supabaseResponse.refreshToken())
                    .role(null)
                    .name(null)
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("refresh_token_invalid_local ip={} reason={}", ipAddress, e.getMessage());
            throw new InvalidCredentialsException("Token de refresco inválido o expirado");
        } catch (Exception e) {
            log.warn("refresh_token_invalid_via_supabase ip={} reason={}", ipAddress, e.getMessage());
            throw new InvalidCredentialsException("Token de refresco inválido o expirado");
        }
    }
}