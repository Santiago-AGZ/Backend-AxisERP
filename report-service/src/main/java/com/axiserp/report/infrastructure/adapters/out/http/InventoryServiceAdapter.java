package com.axiserp.report.infrastructure.adapters.out.http;

import com.axiserp.report.ports.output.InventoryServicePort;
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
public class InventoryServiceAdapter implements InventoryServicePort {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String baseUrl;

    @Value("${internal.api.key}")
    private String apiKey;

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    @Override
    public JsonNode fetchProducts(int page, int size, UUID categoryId) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/inventory/products")
                .queryParam("page", page)
                .queryParam("size", size);
        if (categoryId != null) uri.queryParam("categoryId", categoryId);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchAlerts(int page, int size) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/inventory/alerts")
                .queryParam("page", page)
                .queryParam("size", size);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchProductById(UUID productId) {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                baseUrl + "/api/v1/inventory/products/" + productId,
                HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }
}
