package com.axiserp.auth.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.JwtService;
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
    private final JwtService jwtService;

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
            log.info("refresh_token_not_in_db_attempting_supabase_fallback ip={}", ipAddress);
            return refreshViaSupabaseFallback(refreshTokenValue, ipAddress, userAgent);
        } catch (Exception e) {
            log.warn("refresh_token_invalid_via_supabase ip={} reason={}", ipAddress, e.getMessage());
            throw new InvalidCredentialsException("Token de refresco inválido o expirado");
        }
    }

    private LoginResponse refreshViaSupabaseFallback(String refreshTokenValue, String ipAddress, String userAgent) {
        try {
            var supabaseResponse = supabaseAuthPort.refreshToken(refreshTokenValue);

            String userIdStr = jwtService.getUserIdFromToken(supabaseResponse.accessToken());
            UUID userId = UUID.fromString(userIdStr);

            refreshTokenService.revoke(refreshTokenValue);
            refreshTokenService.saveExternalToken(userId, supabaseResponse.refreshToken(), ipAddress, userAgent);

            log.info("token_refreshed_via_fallback user_id={} ip={}", userId, ipAddress);

            return LoginResponse.builder()
                    .accessToken(supabaseResponse.accessToken())
                    .refreshToken(supabaseResponse.refreshToken())
                    .role(null)
                    .name(null)
                    .build();

        } catch (Exception e) {
            log.warn("refresh_token_fallback_failed ip={} reason={}", ipAddress, e.getMessage());
            throw new InvalidCredentialsException("Token de refresco inválido o expirado");
        }
    }
}