package com.axiserp.auth.infrastructure.config;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 99)
@RequiredArgsConstructor
public class FirstLoginFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirstLoginFilter.class);

    private final UserRepositoryPort userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String userIdStr) {
            if (auth.getCredentials() instanceof Jwt jwt) {
                Instant emailConfirmed = jwt.getClaimAsInstant("email_confirmed_at");
                if (emailConfirmed != null) {
                    activateIfPending(UUID.fromString(userIdStr));
                }
            } else {
                activateIfPending(UUID.fromString(userIdStr));
            }
        }

        chain.doFilter(request, response);
    }

    private void activateIfPending(UUID userId) {
        userRepository.findById(userId)
                .filter(user -> UserStatus.PENDIENTE.equals(user.getStatus()))
                .ifPresent(user -> {
                    User promoted = UserFactory.withSuccessfulLogin(user);
                    promoted.setStatus(UserStatus.ACTIVO);
                    userRepository.save(promoted);
                    log.info("user_activated_on_first_login user_id={}", userId);
                });
    }
}
