package com.axiserp.auth.infrastructure.adapters.out.supabase;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SupabaseAdminAdapter implements SupabaseAuthPort {

    private final RestClient restClient;

    public SupabaseAdminAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {

        String baseUrl = supabaseUrl + "/auth/v1/admin";

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", serviceRoleKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public SupabaseUser createUser(String email, String roleName, String name, UUID createdBy) {
        var body = Map.of(
            "email", email,
            "app_metadata", Map.of("role", roleName),
            "user_metadata", Map.of(
                "name", name,
                "createdBy", createdBy.toString()
            ),
            "email_confirm", false
        );

        JsonNode response = restClient.post()
                .uri("/users")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return new SupabaseUser(
                UUID.fromString(response.get("id").asText()),
                response.get("email").asText(),
                Instant.parse(response.get("invited_at").asText()));
    }
}
