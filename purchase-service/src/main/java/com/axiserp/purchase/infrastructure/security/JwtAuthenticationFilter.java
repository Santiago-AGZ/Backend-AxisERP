package com.axiserp.purchase.infrastructure.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

    private final RestClient restClient;
    private final String jwksUrl;
    private final String internalApiKey;

    private volatile PublicKey cachedPublicKey;
    private volatile String cachedKeyId;

    public JwtAuthenticationFilter(
            RestClient.Builder restClientBuilder,
            @Value("${jwt.jwks-uri}") String jwksUrl,
            @Value("${internal-api-key:}") String internalApiKey) {
        this.jwksUrl = jwksUrl;
        this.internalApiKey = internalApiKey;
        this.restClient = restClientBuilder.build();
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String internalKey = request.getHeader("X-Internal-Api-Key");
        if (internalKey != null && !internalApiKey.isBlank() && internalApiKey.equals(internalKey)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    SYSTEM_USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("internal_api_key_auth_success path={}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            var header = parseHeader(token);
            String kid = (String) header.get("kid");
            String alg = (String) header.get("alg");

            PublicKey key = getPublicKey(kid, alg);
            if (key == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = null;
            var appMeta = claims.get("app_metadata", Map.class);
            if (appMeta != null && appMeta.containsKey("role")) {
                role = String.valueOf(appMeta.get("role"));
            }
            if (role == null || role.isBlank()) {
                role = claims.get("role", String.class);
            }
            if (role == null || role.isBlank()) {
                role = "VENDEDOR";
            }

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("jwt_auth_success user_id={} role={} path={}", userId, role, request.getRequestURI());
        } catch (JwtException e) {
            log.debug("jwt_auth_failed path={} reason={}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private PublicKey getPublicKey(String kid, String alg) {
        if (kid != null && kid.equals(cachedKeyId) && cachedPublicKey != null) {
            return cachedPublicKey;
        }
        try {
            var jwks = restClient.get().uri(jwksUrl).retrieve().body(Map.class);
            if (jwks == null || !jwks.containsKey("keys")) return null;
            @SuppressWarnings("unchecked")
            var keys = (List<Map<String, Object>>) jwks.get("keys");
            for (var key : keys) {
                if ((kid == null || kid.equals(key.get("kid"))) && alg != null && alg.equals(key.get("alg"))) {
                    PublicKey pk = buildEcPublicKey(key);
                    cachedPublicKey = pk;
                    cachedKeyId = (String) key.get("kid");
                    return pk;
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch JWKS: {}", e.getMessage());
        }
        return null;
    }

    private PublicKey buildEcPublicKey(Map<String, Object> jwk) throws Exception {
        String x = (String) jwk.get("x");
        String y = (String) jwk.get("y");
        String crv = (String) jwk.get("crv");
        String javaCurve = "P-256".equals(crv) ? "secp256r1" : crv;

        var spec = java.security.spec.ECGenParameterSpec.class.getDeclaredConstructor(String.class).newInstance(javaCurve);
        var kpg = java.security.KeyPairGenerator.getInstance("EC");
        kpg.initialize(spec);
        var params = ((java.security.interfaces.ECPublicKey) kpg.generateKeyPair().getPublic()).getParams();

        var xBytes = Base64.getUrlDecoder().decode(pad(x));
        var yBytes = Base64.getUrlDecoder().decode(pad(y));
        var point = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
        var pubSpec = new ECPublicKeySpec(point, params);
        return KeyFactory.getInstance("EC").generatePublic(pubSpec);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseHeader(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) return Map.of();
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(pad(parts[0]));
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(decoded, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String pad(String base64) {
        int padding = 4 - (base64.length() % 4);
        if (padding == 4) return base64;
        return base64 + "=".repeat(padding);
    }
}


