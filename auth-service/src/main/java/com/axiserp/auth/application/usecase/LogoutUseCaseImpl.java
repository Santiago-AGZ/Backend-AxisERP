package com.axiserp.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.domain.model.TokenBlacklist;
import com.axiserp.auth.ports.input.LogoutUseCase;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogoutUseCaseImpl.class);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        Claims claims;
        try {
            claims = jwtService.parseToken(refreshTokenValue);
        } catch (Exception e) {
            log.warn("logout_token_invalid");
            return;
        }

        String userId = claims.getSubject();

        RefreshToken storedToken = refreshTokenRepositoryPort.findByToken(refreshTokenValue)
                .orElse(null);

        if (storedToken != null) {
            storedToken.setStatus(RefreshToken.TokenStatus.REVOKED);
            storedToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepositoryPort.save(storedToken);
        }

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(refreshTokenValue)
                .tokenType("refresh")
                .userId(UUID.fromString(userId))
                .reason("LOGOUT")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        tokenBlacklistRepositoryPort.save(blacklist);

        userRepositoryPort.findById(UUID.fromString(userId))
                .ifPresent(user -> {
                    auditService.logLogout(user.getId(), user.getName(), null, null);
                });

        log.info("logout_success user_id={}", userId);
    }
}
