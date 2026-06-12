package com.axiserp.auth.application.service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio para generación, validación y parsing de tokens JWT.
 * Utiliza el algoritmo HS256 con una secret key de mínimo 256 bits.
 * RNF-001: Access Token exp=15min, Refresh Token exp=7d.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt-secret}") String secret,
            @Value("${jwt-access-expiration}") long accessTokenExpiration,
            @Value("${jwt-refresh-expiration}") long refreshTokenExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(java.util.HexFormat.of().parseHex(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        log.info("jwt_service_initialized algorithm=HS256 access_expiration_ms={} refresh_expiration_ms={}",
                accessTokenExpiration, refreshTokenExpiration);
    }

    /**
     * Generates an access token with 15-minute expiration.
     *
     * @param userId identifier of the user
     * @param role role of the user
     * @return signed JWT token
     */
    public String generateAccessToken(String userId, String role) {
        return buildToken(userId, role, accessTokenExpiration);
    }

    /**
     * Generates a refresh token with 7-day expiration.
     *
     * @param userId identifier of the user
     * @param role role of the user
     * @return signed JWT token
     */
    public String generateRefreshToken(String userId, String role) {
        return buildToken(userId, role, refreshTokenExpiration);
    }

    private String buildToken(String userId, String role, long expiration) {
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("role", role))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a JWT token, returning its claims.
     *
     * @param token JWT token to parse
     * @return claims of the token
     * @throws JwtException if the token is invalid or expired
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user_id from the token subject.
     *
     * @param token JWT token
     * @return user_id as string
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extracts the role from the "role" claim.
     *
     * @param token JWT token
     * @return role of the user
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Validates that a JWT token is current and correctly signed.
     *
     * @param token JWT token to validate
     * @return true if valid, false if expired or corrupt
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(Date.from(Instant.now()));
        } catch (JwtException e) {
            log.debug("invalid_jwt_token reason={}", e.getMessage());
            return false;
        }
    }
}
