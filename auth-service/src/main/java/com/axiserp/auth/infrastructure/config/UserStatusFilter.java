package com.axiserp.auth.infrastructure.config;

import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.infrastructure.config.dto.JitProvisionResult;
import com.axiserp.auth.application.service.TokenBlacklistService;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 98)
@RequiredArgsConstructor
public class UserStatusFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserStatusFilter.class);

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

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
            if ("anonymousUser".equals(userId)) {
                chain.doFilter(request, response);
                return;
            }
            // Validar si el token ha sido revocado
            if (auth.getCredentials() instanceof Jwt jwt) {
                String tokenJti = jwt.getId();
                if (tokenBlacklistService.isRevoked(tokenJti)) {
                    throw new UserInactiveException("Token ha sido revocado. Inicie sesión nuevamente.");
                }
            }

            UUID uuid = UUID.fromString(userId);
            JitProvisionResult result = findOrProvision(uuid, auth);

            if (!result.user().isActive()) {
                throw new UserInactiveException(
                        "Usuario " + result.user().getStatus().name().toLowerCase()
                        + (result.user().getDeletedAt() != null ? " o eliminado" : "")
                        + ". No tiene permisos para acceder al sistema.");
            }
        }

        chain.doFilter(request, response);
    }

    private JitProvisionResult findOrProvision(UUID userId, Authentication auth) {
        var existing = userRepository.findById(userId);
        if (existing.isPresent()) {
            return new JitProvisionResult(existing.get(), false);
        }

        if (auth.getCredentials() instanceof Jwt jwt) {
            return provisionFromJwt(userId, jwt);
        }

        if (auth.getCredentials() instanceof String token) {
            try {
                Claims claims = jwtService.parseToken(token);
                Jwt jwt = Jwt.withTokenValue(token)
                        .header("alg", "HS256")
                        .claim("sub", claims.getSubject())
                        .claim("email", claims.get("email", String.class))
                        .claim("role", claims.get("role", String.class))
                        .build();
                return provisionFromJwt(userId, jwt);
            } catch (Exception e) {
                log.warn("jit_provision_parse_failed user_id={} error={}", userId, e.getMessage());
            }
        }

        log.warn("jit_provision_fallback user_id={}", userId);
        User fallback = UserFactory.createNew(userId, userId.toString(), null, null, null, null);
        fallback.setStatus(User.UserStatus.ACTIVO);
        User saved = userRepository.save(fallback);
        return new JitProvisionResult(saved, true);
    }

    private JitProvisionResult provisionFromJwt(UUID userId, Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String name = extractName(jwt);
        String roleName = extractRole(jwt);
        var role = roleRepository.findByName(roleName).orElse(null);
        UUID roleId = role != null ? role.getId() : null;

        User provisioned = UserFactory.createNew(userId, name, email, null, roleId, null);
        provisioned.setStatus(User.UserStatus.ACTIVO);
        User saved = userRepository.save(provisioned);

        log.info("jit_provision id={} email={} role={}", saved.getId(), saved.getEmail(), roleName);
        return new JitProvisionResult(saved, true);
    }

    private String extractName(Jwt jwt) {
        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");
        if (userMetadata != null && userMetadata.containsKey("name")) {
            return (String) userMetadata.get("name");
        }
        return jwt.getClaimAsString("email");
    }

    private String extractRole(Jwt jwt) {
        try {
            Map<String, Object> appMetadata = jwt.getClaimAsMap("app_metadata");
            if (appMetadata != null && appMetadata.containsKey("role")) {
                return (String) appMetadata.get("role");
            }
        } catch (Exception e) {
            // default
        }
        return "INVENTARIO";
    }
}
