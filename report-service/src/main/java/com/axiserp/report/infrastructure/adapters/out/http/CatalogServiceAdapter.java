package com.axiserp.report.infrastructure.adapters.out.http;

import com.axiserp.report.ports.output.CatalogServicePort;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogServiceAdapter implements CatalogServicePort {

    private final RestTemplate restTemplate;

    @Value("${catalog-service-url}")
    private String baseUrl;

    @Value("${internal-api-key}")
    private String apiKey;

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    @Override
    public JsonNode fetchProducts(String search, UUID categoryId, boolean includeInactive, int page, int size) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/productos")
                .queryParam("page", page)
                .queryParam("size", size);
        if (search != null && !search.isBlank()) uri.queryParam("search", search);
        if (categoryId != null) uri.queryParam("categoryId", categoryId);
        if (includeInactive) uri.queryParam("includeInactive", true);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchCategories(String search, boolean includeInactive, int page, int size) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/categorias")
                .queryParam("page", page)
                .queryParam("size", size);
        if (search != null && !search.isBlank()) uri.queryParam("search", search);
        if (includeInactive) uri.queryParam("includeInactive", true);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }
}
