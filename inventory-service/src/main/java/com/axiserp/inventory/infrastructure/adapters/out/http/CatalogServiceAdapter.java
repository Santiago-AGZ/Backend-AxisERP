package com.axiserp.inventory.infrastructure.adapters.out.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.ports.output.CatalogServicePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogServiceAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${catalog.service.url}")
    private String catalogServiceUrl;

    @Value("${internal.api.key:}")
    private String internalApiKey;

    @Override
    public List<UUID> findProductIdsByCategoryId(UUID categoryId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(catalogServiceUrl)
                .path("/api/v1/productos")
                .queryParam("categoryId", categoryId)
                .toUriString();

        log.info("catalog_fetch_by_category categoryId={}", categoryId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            List<UUID> productIds = extractProductIds(response.getBody());

            log.info("catalog_fetch_success categoryId={} productsFound={}", categoryId, productIds.size());
            return productIds;
        } catch (Exception e) {
            log.error("catalog_fetch_error categoryId={} error={}", categoryId, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<UUID> extractProductIds(Map<String, Object> body) {
        if (body == null) {
            return List.of();
        }
        Object dataObj = body.get("data");
        if (dataObj instanceof List<?> productList) {
            List<UUID> ids = new ArrayList<>();
            for (Object item : productList) {
                if (item instanceof Map<?, ?> product) {
                    Object idObj = product.get("id");
                    if (idObj instanceof String idStr) {
                        try {
                            ids.add(UUID.fromString(idStr));
                        } catch (IllegalArgumentException e) {
                            log.warn("invalid_uuid_in_catalog_response id={}", idStr);
                        }
                    }
                }
            }
            return ids;
        }
        return List.of();
    }

    @Override
    public Map<UUID, ProductSummary> findProductSummaries(List<UUID> productIds) {
        Map<UUID, ProductSummary> summaries = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return summaries;
        }
        for (UUID productId : productIds) {
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl(catalogServiceUrl)
                        .path("/api/v1/productos/{id}")
                        .buildAndExpand(productId)
                        .toUriString();

                HttpHeaders headers = buildHeaders();
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

                ProductSummary summary = extractProductSummary(response.getBody());
                if (summary != null) {
                    summaries.put(productId, summary);
                }
            } catch (Exception e) {
                log.warn("catalog_fetch_product_error productId={} error={}", productId, e.getMessage());
            }
        }
        log.info("catalog_fetch_summaries requested={} resolved={}", productIds.size(), summaries.size());
        return summaries;
    }

    @SuppressWarnings("unchecked")
    private ProductSummary extractProductSummary(Map<String, Object> body) {
        if (body == null) {
            return null;
        }
        Object dataObj = body.get("data");
        if (dataObj instanceof Map<?, ?> product) {
            Object idObj = product.get("id");
            Object nameObj = product.get("name");
            Object codigoObj = product.get("codigo");
            Object statusObj = product.get("status");
            if (idObj instanceof String idStr && nameObj instanceof String name) {
                String codigo = codigoObj instanceof String c ? c : null;
                String status = statusObj instanceof String s ? s : null;
                return ProductSummary.builder()
                        .id(UUID.fromString(idStr))
                        .name(name)
                        .codigo(codigo)
                        .status(status)
                        .build();
            }
        }
        return null;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            headers.set("X-Internal-Api-Key", internalApiKey);
        }
        return headers;
    }
}
