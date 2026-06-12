package com.axiserp.sales.infrastructure.adapters.out.http;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.axiserp.sales.application.dto.response.ProductSummary;
import com.axiserp.sales.ports.output.CatalogServicePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogServiceAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${catalog-service-url}")
    private String catalogServiceUrl;

    @Value("${internal-api-key:}")
    private String internalApiKey;

    @Override
    public boolean productExists(UUID productId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(catalogServiceUrl)
                .path("/api/v1/productos/{id}")
                .buildAndExpand(productId)
                .toUriString();

        log.info("catalog_check_exists productId={}", productId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("catalog_product_not_found productId={}", productId);
            return false;
        } catch (Exception e) {
            log.error("catalog_check_error productId={} error={}", productId, e.getMessage());
            return false;
        }
    }

    @Override
    public ProductSummary findProductSummary(UUID productId) {
        String url = UriComponentsBuilder
                .fromHttpUrl(catalogServiceUrl)
                .path("/api/v1/productos/{id}")
                .buildAndExpand(productId)
                .toUriString();

        log.info("catalog_fetch_summary productId={}", productId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            return extractProductSummary(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("catalog_product_not_found productId={}", productId);
            return null;
        } catch (Exception e) {
            log.error("catalog_fetch_summary_error productId={} error={}", productId, e.getMessage());
            return null;
        }
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