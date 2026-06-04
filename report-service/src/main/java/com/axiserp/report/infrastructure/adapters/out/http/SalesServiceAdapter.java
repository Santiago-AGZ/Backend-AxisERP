package com.axiserp.report.infrastructure.adapters.out.http;

import com.axiserp.report.ports.output.SalesServicePort;
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

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SalesServiceAdapter implements SalesServicePort {

    private final RestTemplate restTemplate;

    @Value("${sales.service.url}")
    private String baseUrl;

    @Value("${internal.api.key}")
    private String apiKey;

    private HttpEntity<Void> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", apiKey);
        return new HttpEntity<>(headers);
    }

    @Override
    public JsonNode fetchSales(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId, int page, int size) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/sales")
                .queryParam("page", page)
                .queryParam("size", size);
        if (startDate != null) uri.queryParam("startDate", startDate);
        if (endDate != null) uri.queryParam("endDate", endDate);
        if (status != null && !status.isBlank()) uri.queryParam("status", status);
        if (userId != null) uri.queryParam("userId", userId);
        if (clientId != null) uri.queryParam("clientId", clientId);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchCustomers(String search, boolean includeInactive, int page, int size) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/customers")
                .queryParam("page", page)
                .queryParam("size", size);
        if (search != null && !search.isBlank()) uri.queryParam("search", search);
        if (includeInactive) uri.queryParam("includeInactive", true);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchCustomerById(UUID customerId) {
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                baseUrl + "/api/v1/customers/" + customerId,
                HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }

    @Override
    public JsonNode fetchSalesByIds(String saleIds) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/sales")
                .queryParam("ids", saleIds)
                .queryParam("page", 1)
                .queryParam("size", 100);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, createAuthEntity(), JsonNode.class);
        return response.getBody();
    }
}
