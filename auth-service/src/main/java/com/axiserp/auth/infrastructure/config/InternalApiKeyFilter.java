package com.axiserp.auth.infrastructure.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro que permite autenticacion inter-servicio mediante API key interna.
 * Los microservicios (catalog, inventory, purchase, sales) usan este mecanismo
 * para consultar el auth-service sin necesidad de token JWT de usuario.
 *
 * El header X-Internal-Api-Key debe coincidir con la variable INTERNAL_API_KEY.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);
    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

    private final String internalApiKey;

    public InternalApiKeyFilter(@Value("${internal.api.key:}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader("X-Internal-Api-Key");
        if (key != null && !internalApiKey.isBlank() && internalApiKey.equals(key)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    SYSTEM_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("internal_api_key_auth path={}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
