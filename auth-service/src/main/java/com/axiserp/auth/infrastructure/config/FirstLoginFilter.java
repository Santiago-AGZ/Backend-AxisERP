package com.axiserp.auth.infrastructure.config;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FirstLoginFilter extends OncePerRequestFilter {

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

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String userId) {
            if (auth.getCredentials() instanceof Jwt jwt) {
                Instant emailConfirmed = jwt.getClaimAsInstant("email_confirmed_at");

                if (emailConfirmed != null) {
                    userRepository.findById(UUID.fromString(userId))
                            .filter(user -> UserStatus.PENDIENTE.equals(user.getStatus()))
                            .ifPresent(user -> {
                                user.setStatus(UserStatus.ACTIVO);
                                userRepository.save(user);
                            });
                }
            }
        }

        chain.doFilter(request, response);
    }
}
