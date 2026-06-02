package com.axiserp.auth.application.usecase;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.LoginRequest;
import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.domain.exception.InvalidCredentialsException;
import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.exception.UserLockedException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.RefreshToken;
import com.axiserp.auth.domain.model.RefreshToken.TokenStatus;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.service.LoginRateLimitStrategy;
import com.axiserp.auth.ports.input.AuthenticateUserUseCase;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Implementacion del caso de uso de autenticacion (HU-001).
 * Valida credenciales, genera tokens JWT, aplica rate limiting y registra auditoria.
 */
@Service
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUserService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimitStrategy rateLimitStrategy;

    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepositoryPort.findByEmailOrName(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            log.warn("login_attempt_inactive_user username={}", request.getUsername());
            throw new UserInactiveException();
        }

        if (!rateLimitStrategy.isLoginAllowed(user)) {
            log.warn("login_blocked_rate_limit username={} attempts={}",
                    request.getUsername(), user.getFailedLoginAttempts());
            throw new UserLockedException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            User updated = rateLimitStrategy.recordFailedAttempt(user);
            userRepositoryPort.save(updated);
            log.warn("login_failed_invalid_password username={} ip={} attempts_left={}",
                    request.getUsername(), ipAddress, rateLimitStrategy.remainingAttempts(updated));
            auditService.logLogin(user.getId(), user.getName(), false, ipAddress, userAgent);
            throw new InvalidCredentialsException();
        }

        String roleName = roleRepositoryPort.findById(user.getRoleId())
                .map(Role -> Role.getName())
                .orElseThrow(() -> {
                    log.error("login_role_not_found user_id={} role_id={}", user.getId(), user.getRoleId());
                    return new IllegalStateException("Role not found for user");
                });

        User afterLogin = rateLimitStrategy.recordSuccessfulLogin(user);
        userRepositoryPort.save(afterLogin);

        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(), roleName);
        String refreshTokenValue = jwtService.generateRefreshToken(
                user.getId().toString(), roleName);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshTokenValue)
                .status(TokenStatus.ACTIVE)
                .expiresAt(java.time.LocalDateTime.now().plusSeconds(604800))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        refreshTokenRepositoryPort.save(refreshToken);

        auditService.logLogin(user.getId(), user.getName(), true, ipAddress, userAgent);
        log.info("login_success user_id={} username={} role={} ip={}",
                user.getId(), request.getUsername(), roleName, ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .role(roleName)
                .name(user.getName())
                .build();
    }
}
