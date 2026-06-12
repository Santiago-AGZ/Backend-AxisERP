package com.axiserp.auth.infrastructure.adapters.out.supabase;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpStatusCodeException;

import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SupabaseAdminAdapter implements SupabaseAuthPort {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAdminAdapter.class);

    private final RestClient restClient;
    private final RestClient publicRestClient;

    public SupabaseAdminAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${supabase-url}") String supabaseUrl,
            @Value("${supabase-service-role-key}") String serviceRoleKey,
            @Value("${supabase-anon-key}") String anonKey) {

        String baseUrl = supabaseUrl + "/auth/v1/admin";

        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        this.publicRestClient = restClientBuilder.clone()
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", anonKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public void sendPasswordReset(String email) {
        log.info("Sending password reset email to: {}", email);
        try {
            publicRestClient.post()
                    .uri("/recover")
                    .body(Map.of("email", email))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            log.warn("Password reset API responded: {}", e.getStatusCode());
        }
    }

    @Override
    public LoginResponse login(String email, String password) {
        log.info("Logging in via Supabase: email={}", email);
        try {
            JsonNode response = publicRestClient.post()
                    .uri("/token?grant_type=password")
                    .body(Map.of("email", email, "password", password))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new RuntimeException("Supabase returned empty response for login");
            }

            String accessToken = response.get("access_token").asText();
            String refreshToken = response.get("refresh_token").asText();
            int expiresIn = response.get("expires_in").asInt();
            String tokenType = response.get("token_type").asText();

            log.debug("Login successful, expires_in={}", expiresIn);
            return new LoginResponse(accessToken, refreshToken, expiresIn, tokenType);

        } catch (HttpStatusCodeException e) {
            log.error("Supabase API error logging in: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().isSameCodeAs(org.springframework.http.HttpStatus.BAD_REQUEST)) {
                throw new IllegalArgumentException("Credenciales inválidas");
            }
            throw new RuntimeException(
                    "Error al iniciar sesión en Supabase: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Supabase API call failed during login", e);
            throw new RuntimeException("Error de comunicación con Supabase al iniciar sesión", e);
        }
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {
        log.info("Refreshing token via Supabase");
        try {
            JsonNode response = publicRestClient.post()
                    .uri("/token?grant_type=refresh_token")
                    .body(Map.of("refresh_token", refreshToken))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new RuntimeException("Supabase returned empty response for token refresh");
            }

            String newAccessToken = response.get("access_token").asText();
            String newRefreshToken = response.get("refresh_token").asText();
            int expiresIn = response.get("expires_in").asInt();

            log.debug("Token refreshed successfully, expires_in={}", expiresIn);
            return new RefreshTokenResponse(newAccessToken, newRefreshToken, expiresIn);

        } catch (HttpStatusCodeException e) {
            log.error("Supabase API error refreshing token: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error al refrescar token en Supabase: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Supabase API call failed during token refresh", e);
            throw new RuntimeException("Error de comunicación con Supabase al refrescar token", e);
        }
    }

    @Override
    public SupabaseUser createUser(String email, String roleName, String name, UUID createdBy) {
        log.info("Creating Supabase user: email={}", email);

        var body = Map.of(
            "email", email,
            "app_metadata", Map.of("role", roleName),
            "user_metadata", Map.of(
                "name", name,
                "nombre", name,
                "createdBy", createdBy.toString()
            ),
            "email_confirm", false
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/users")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new RuntimeException("Supabase returned empty response for user creation: email=" + email);
            }

            JsonNode idNode = response.get("id");
            JsonNode emailNode = response.get("email");
            JsonNode invitedAtNode = response.get("invited_at");
            JsonNode createdAtNode = response.get("created_at");

            if (idNode == null || idNode.isNull()) {
                throw new RuntimeException("Missing 'id' in Supabase response: " + response);
            }
            if (emailNode == null || emailNode.isNull()) {
                throw new RuntimeException("Missing 'email' in Supabase response: " + response);
            }

            Instant invitedAt = (invitedAtNode != null && !invitedAtNode.isNull())
                    ? Instant.parse(invitedAtNode.asText())
                    : (createdAtNode != null && !createdAtNode.isNull()
                            ? Instant.parse(createdAtNode.asText())
                            : Instant.now());

            SupabaseUser supabaseUser = new SupabaseUser(
                    UUID.fromString(idNode.asText()),
                    emailNode.asText(),
                    invitedAt);

            log.debug("Supabase user created: id={} email={}", supabaseUser.id(), supabaseUser.email());
            return supabaseUser;

        } catch (HttpStatusCodeException e) {
            log.error("Supabase API error creating user: email={} status={} body={}",
                    email, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(
                    "Error al crear usuario en Supabase: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    e);
        } catch (Exception e) {
            log.error("Supabase API call failed: email={}", email, e);
            throw new RuntimeException("Error de comunicación con Supabase al crear usuario", e);
        }
    }
}
