package com.axiserp.purchase.infrastructure.adapters.out.catalog;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.axiserp.purchase.ports.output.CatalogServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class CatalogServiceAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceAdapter.class);
    private static final String CB_NAME = "catalogService";

    private final RestClient restClient;
    private final String internalApiKey;

    public CatalogServiceAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${catalog.service.url:http://catalog-service:8082}") String catalogUrl,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
        this.restClient = restClientBuilder
                .baseUrl(catalogUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    @Override
    @Retry(name = CB_NAME)
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "productExistsFallback")
    public boolean productExists(UUID productId) {
        try {
            restClient.get()
                    .uri("/api/v1/productos/{id}", productId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("catalog_product_not_found productId={}", productId);
            return false;
        }
    }

    @SuppressWarnings("unused")
    private boolean productExistsFallback(UUID productId, Throwable t) {
        log.warn("circuit_breaker_fallback catalogService productId={} error={}", productId, t.getMessage());
        return true; // fail-open: si catalog esta caido, permitir la compra
    }
}
