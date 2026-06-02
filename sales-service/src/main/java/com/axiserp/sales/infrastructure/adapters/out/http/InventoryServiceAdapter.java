package com.axiserp.sales.infrastructure.adapters.out.http;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.axiserp.sales.domain.exception.InsufficientStockException;
import com.axiserp.sales.ports.output.InventoryServicePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryServiceAdapter implements InventoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${internal.api.key:}")
    private String internalApiKey;

    @Override
    public void checkAndExit(UUID productId, int quantity, String referenceType, UUID referenceId, String notes) {
        String url = UriComponentsBuilder
                .fromHttpUrl(inventoryServiceUrl)
                .path("/api/v1/inventory/products/{productId}/exit")
                .queryParam("quantity", quantity)
                .queryParam("referenceType", referenceType)
                .queryParam("referenceId", referenceId)
                .buildAndExpand(productId)
                .toUriString();

        log.info("inventory_exit_request productId={} quantity={} saleId={}", productId, quantity, referenceId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("inventory_exit_success productId={} status={}", productId, response.getStatusCode());
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("inventory_exit_insufficient_stock productId={} reason={}", productId, e.getMessage());
            throw new InsufficientStockException(productId);
        } catch (HttpClientErrorException e) {
            log.error("inventory_exit_client_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new InsufficientStockException("Error al descontar stock para el producto: " + productId + ". " + e.getMessage());
        }
    }

    @Override
    public void registerReturn(UUID productId, int quantity, String referenceType, UUID referenceId, String notes) {
        String url = UriComponentsBuilder
                .fromHttpUrl(inventoryServiceUrl)
                .path("/api/v1/inventory/products/{productId}/return")
                .queryParam("quantity", quantity)
                .queryParam("referenceType", referenceType)
                .queryParam("referenceId", referenceId)
                .buildAndExpand(productId)
                .toUriString();

        log.info("inventory_return_request productId={} quantity={} saleId={}", productId, quantity, referenceId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("inventory_return_success productId={} status={}", productId, response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("inventory_return_client_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al registrar devolucion de stock para el producto: " + productId + ". " + e.getMessage(), e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            headers.set("X-Internal-Api-Key", internalApiKey);
        }
        return headers;
    }
}
