package com.axiserp.sales.infrastructure.adapters.out.http;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

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
        String url = inventoryServiceUrl + "/api/v1/inventory/products/" + productId + "/exit";

        log.info("inventory_exit_request productId={} quantity={} saleId={}", productId, quantity, referenceId);

        try {
            HttpHeaders headers = buildHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "quantity", quantity,
                    "referenceType", referenceType,
                    "referenceId", referenceId != null ? referenceId.toString() : null);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            log.info("inventory_exit_success productId={} status={}", productId, response.getStatusCode());
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("inventory_exit_insufficient_stock productId={} reason={}", productId, e.getMessage());
            throw new InsufficientStockException(productId);
        } catch (HttpClientErrorException e) {
            log.error("inventory_exit_client_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new InsufficientStockException("Error al descontar stock para el producto: " + productId + ". " + e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("inventory_exit_connection_error productId={} url={} error={}", productId, url, e.getMessage());
            throw new InsufficientStockException("Error de conexión con el servicio de inventario para el producto: " + productId + ". Verifique que el servicio de inventario esté disponible.");
        } catch (HttpServerErrorException e) {
            log.error("inventory_exit_server_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new InsufficientStockException("Error del servidor de inventario para el producto: " + productId + ". El servicio de inventario respondió con error.");
        }
    }

    @Override
    public void registerReturn(UUID productId, int quantity, String referenceType, UUID referenceId, String notes) {
        String url = inventoryServiceUrl + "/api/v1/inventory/products/" + productId + "/return";

        log.info("inventory_return_request productId={} quantity={} saleId={}", productId, quantity, referenceId);

        try {
            HttpHeaders headers = buildHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "quantity", quantity,
                    "referenceType", referenceType,
                    "referenceId", referenceId != null ? referenceId.toString() : null);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
            log.info("inventory_return_success productId={} status={}", productId, response.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("inventory_return_client_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al registrar devolucion de stock para el producto: " + productId + ". " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("inventory_return_connection_error productId={} url={} error={}", productId, url, e.getMessage());
            throw new RuntimeException("Error de conexión con el servicio de inventario al registrar devolución para el producto: " + productId, e);
        } catch (HttpServerErrorException e) {
            log.error("inventory_return_server_error productId={} status={} body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error del servidor de inventario al registrar devolución para el producto: " + productId, e);
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
