package com.axiserp.purchase.infrastructure.adapters.out.catalog;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.axiserp.purchase.ports.output.CatalogServicePort;

@Component
public class CatalogServiceAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceAdapter.class);

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
    public boolean productExists(UUID productId) {
        try {
            restClient.get()
                    .uri("/api/v1/productos/{id}", productId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("product_not_found productId={}", productId);
            return false;
        }
    }
}
