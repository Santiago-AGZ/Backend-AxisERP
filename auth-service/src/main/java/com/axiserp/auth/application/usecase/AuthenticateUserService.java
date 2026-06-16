package com.axiserp.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.request.LoginRequest;
import com.axiserp.auth.application.dto.response.LoginResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.application.service.SuspiciousActivityDetector;
import com.axiserp.auth.domain.exception.InvalidCredentialsException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.exception.UserLockedException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.service.LoginRateLimitStrategy;
import com.axiserp.auth.ports.input.AuthenticateUserUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUserService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final SupabaseAuthPort supabaseAuthPort;
    private final AuditService auditService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimitStrategy rateLimitStrategy;
    private final SuspiciousActivityDetector suspiciousActivityDetector;
    private final PasswordEncoder passwordEncoder;

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

        // Authenticate via Supabase Auth API
        try {
            var supabaseResponse = supabaseAuthPort.login(request.getUsername(), request.getPassword());

            String roleName = roleRepositoryPort.findById(user.getRoleId())
                    .map(role -> role.getName())
                    .orElseThrow(() -> {
                        log.error("login_role_not_found user_id={} role_id={}", user.getId(), user.getRoleId());
                        return new IllegalStateException("Role not found for user");
                    });

            User afterLogin = rateLimitStrategy.recordSuccessfulLogin(user);

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                afterLogin = UserFactory.withNewPassword(afterLogin, passwordEncoder.encode(request.getPassword()));
                log.info("local_password_hash_synced email={}", user.getEmail());
            }

            userRepositoryPort.save(afterLogin);

            auditService.logLogin(user.getId(), user.getName(), true, ipAddress, userAgent);
            refreshTokenService.saveExternalToken(user.getId(), supabaseResponse.refreshToken(), ipAddress, userAgent);
            log.info("login_success user_id={} email={} role={} ip={}",
                    user.getId(), user.getEmail(), roleName, ipAddress);

            return LoginResponse.builder()
                    .accessToken(supabaseResponse.accessToken())
                    .refreshToken(supabaseResponse.refreshToken())
                    .role(roleName)
                    .name(user.getName())
                    .build();

        } catch (Exception e) {
            log.warn("login_failed_via_supabase email={} ip={} reason={}",
                    request.getUsername(), ipAddress, e.getMessage());

            User updated = rateLimitStrategy.recordFailedAttempt(user);
            userRepositoryPort.save(updated);
            suspiciousActivityDetector.recordFailedLogin(user.getId(), user.getEmail(), ipAddress, userAgent);
            auditService.logLogin(user.getId(), user.getName(), false, ipAddress, userAgent);
            throw new InvalidCredentialsException();
        }
    }
}