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

    public SupabaseAdminAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {

        String baseUrl = supabaseUrl + "/auth/v1/admin";

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public SupabaseUser createUser(String email, String roleName, String name, UUID createdBy) {
        log.info("Creating Supabase user: email={}", email);

        var body = Map.of(
            "email", email,
            "app_metadata", Map.of("role", roleName),
            "user_metadata", Map.of(
                "name", name,
                "createdBy", createdBy.toString()
            ),
            "email_confirm", false
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/users")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
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

        if (response == null) {
            throw new RuntimeException("Supabase returned empty response for user creation: email=" + email);
        }

        JsonNode idNode = response.get("id");
        JsonNode emailNode = response.get("email");
        JsonNode invitedAtNode = response.get("invited_at");

        if (idNode == null || idNode.isNull()) {
            throw new RuntimeException("Missing 'id' in Supabase response: " + response);
        }
        if (emailNode == null || emailNode.isNull()) {
            throw new RuntimeException("Missing 'email' in Supabase response: " + response);
        }
        if (invitedAtNode == null || invitedAtNode.isNull()) {
            throw new RuntimeException("Missing 'invited_at' in Supabase response: " + response);
        }

        SupabaseUser supabaseUser = new SupabaseUser(
                UUID.fromString(idNode.asText()),
                emailNode.asText(),
                Instant.parse(invitedAtNode.asText()));

        log.debug("Supabase user created: id={} email={}", supabaseUser.id(), supabaseUser.email());
        return supabaseUser;
    }
}
