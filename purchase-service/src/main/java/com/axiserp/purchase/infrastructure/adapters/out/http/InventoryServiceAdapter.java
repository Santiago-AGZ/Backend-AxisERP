package com.axiserp.purchase.infrastructure.adapters.out.http;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.axiserp.purchase.ports.output.InventoryServicePort;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class InventoryServiceAdapter implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceAdapter.class);
    private static final String CB_NAME = "inventoryService";

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;
    private final String internalApiKey;

    public InventoryServiceAdapter(RestTemplate restTemplate,
            @Value("${inventory.service.url:http://localhost:8087}") String inventoryServiceUrl,
            @Value("${internal.api.key:}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "registerEntryFallback")
    public void registerEntry(UUID productId, int quantity, String referenceType, UUID referenceId, String notes) {
        String url = inventoryServiceUrl
                + "/api/v1/inventory/products/" + productId
                + "/entry?quantity=" + quantity
                + "&referenceType=" + referenceType
                + "&referenceId=" + referenceId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("inventory_entry_sent productId={} qty={} referenceType={} referenceId={}",
                    productId, quantity, referenceType, referenceId);
        } catch (RestClientException e) {
            log.error("inventory_entry_failed productId={} error={}", productId, e.getMessage());
            throw new RuntimeException("Error al registrar entrada en inventario: " + e.getMessage(), e);
        }
    }

    @Override
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "reverseMovementsFallback")
    public void reverseMovements(UUID purchaseId) {
        String url = inventoryServiceUrl
                + "/api/v1/inventory/purchases/" + purchaseId + "/reverse";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("inventory_reverse_sent purchaseId={}", purchaseId);
        } catch (RestClientException e) {
            log.error("inventory_reverse_failed purchaseId={} error={}", purchaseId, e.getMessage());
            throw new RuntimeException("Error al revertir movimientos en inventario: " + e.getMessage(), e);
        }
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
