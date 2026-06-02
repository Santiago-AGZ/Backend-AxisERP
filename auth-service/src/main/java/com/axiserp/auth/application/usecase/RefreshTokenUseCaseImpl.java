package com.axiserp.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.domain.model.RefreshToken.TokenStatus;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.InvalidCredentialsException;
import com.axiserp.auth.domain.exception.TokenExpiredException;
import com.axiserp.auth.ports.input.RefreshTokenUseCase;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCaseImpl.class);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Override
    @Transactional
    public LoginResponse refresh(String refreshTokenValue, String ipAddress, String userAgent) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshTokenValue);
        } catch (Exception e) {
            log.warn("refresh_token_invalid ip={}", ipAddress);
            throw new InvalidCredentialsException("Token de refresco inválido");
        }

        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        if (tokenBlacklistRepositoryPort.isTokenBlacklisted(refreshTokenValue)) {
            log.warn("refresh_token_blacklisted user_id={}", userId);
            throw new InvalidCredentialsException("Token de refresco revocado");
        }

        RefreshToken storedToken = refreshTokenRepositoryPort.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    log.warn("refresh_token_not_found user_id={}", userId);
                    return new InvalidCredentialsException("Token de refresco no encontrado");
                });

        if (storedToken.getStatus() != TokenStatus.ACTIVE) {
            log.warn("refresh_token_not_active user_id={} status={}", userId, storedToken.getStatus());
            throw new InvalidCredentialsException("Token de refresco no activo");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("refresh_token_expired user_id={}", userId);
            storedToken.setStatus(TokenStatus.EXPIRED);
            refreshTokenRepositoryPort.save(storedToken);
            throw new TokenExpiredException();
        }

        storedToken.setStatus(TokenStatus.REVOKED);
        storedToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepositoryPort.save(storedToken);

        User user = userRepositoryPort.findById(UUID.fromString(userId))
                .orElseThrow(() -> {
                    log.error("refresh_user_not_found user_id={}", userId);
                    return new InvalidCredentialsException("Usuario no encontrado");
                });

        if (!user.isActive()) {
            log.warn("refresh_user_inactive user_id={}", userId);
            throw new InvalidCredentialsException("Usuario inactivo");
        }

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(r -> r.getName())
                .orElse("UNKNOWN");

        String newAccessToken = jwtService.generateAccessToken(userId, roleName);
        String newRefreshToken = jwtService.generateRefreshToken(userId, roleName);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .token(newRefreshToken)
                .status(TokenStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusSeconds(604800))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshTokenRepositoryPort.save(newRefreshTokenEntity);

        log.info("refresh_token_success user_id={} ip={}", userId, ipAddress);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(roleName)
                .name(user.getName())
                .build();
    }
}
