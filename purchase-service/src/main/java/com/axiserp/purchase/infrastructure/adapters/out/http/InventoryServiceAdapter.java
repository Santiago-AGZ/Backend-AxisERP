package com.axiserp.purchase.infrastructure.adapters.out.http;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.axiserp.purchase.ports.output.InventoryServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class InventoryServiceAdapter implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceAdapter.class);
    private static final String CB_NAME = "inventoryService";

    private final RestClient restClient;

    public InventoryServiceAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${inventory-service-url:http://inventory-service:8083}") String inventoryServiceUrl,
            @Value("${internal-api-key:}") String internalApiKey) {
        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    @Override
    @Retry(name = CB_NAME)
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "registerEntryFallback")
    public void registerEntry(UUID productId, int quantity, String referenceType, UUID referenceId, String notes) {
        restClient.post()
                .uri("/api/v1/inventory/products/{productId}/entry?quantity={quantity}&referenceType={referenceType}&referenceId={referenceId}",
                        productId, quantity, referenceType, referenceId)
                .retrieve()
                .toBodilessEntity();
        log.info("inventory_entry_sent productId={} qty={} referenceType={} referenceId={}",
                productId, quantity, referenceType, referenceId);
    }

    @Override
    @Retry(name = CB_NAME)
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "reverseMovementsFallback")
    public void reverseMovements(UUID purchaseId) {
        restClient.post()
                .uri("/api/v1/inventory/purchases/{purchaseId}/reverse", purchaseId)
                .retrieve()
                .toBodilessEntity();
        log.info("inventory_reverse_sent purchaseId={}", purchaseId);
    }

    @SuppressWarnings("unused")
    private void registerEntryFallback(UUID productId, int quantity, String referenceType, UUID referenceId, String notes, Throwable t) {
        log.warn("circuit_breaker_fallback inventoryService registerEntry productId={} error={}", productId, t.getMessage());
    }

    @SuppressWarnings("unused")
    private void reverseMovementsFallback(UUID purchaseId, Throwable t) {
        log.warn("circuit_breaker_fallback inventoryService reverseMovements purchaseId={} error={}", purchaseId, t.getMessage());
    }
}
