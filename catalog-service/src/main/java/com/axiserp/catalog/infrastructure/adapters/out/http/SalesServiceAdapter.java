package com.axiserp.catalog.infrastructure.adapters.out.http;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.axiserp.catalog.ports.output.SalesServicePort;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SalesServiceAdapter implements SalesServicePort {

    private static final Logger log = LoggerFactory.getLogger(SalesServiceAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${sales.service.url}")
    private String salesServiceUrl;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Override
    public boolean hasPendingSales(UUID productId) {
        try {
            String url = String.format("%s/api/v1/sales?productId=%s&status=PENDIENTE&page=1&size=1",
                    salesServiceUrl, productId);

            var headers = new HttpHeaders();
            headers.set("X-Internal-Api-Key", internalApiKey);
            var entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class);

            var body = response.getBody();
            if (body != null && body.has("data") && body.get("data").isArray()) {
                return body.get("data").size() > 0;
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to check pending sales for product {}, allowing deactivation", productId, e);
            return false;
        }
    }
}
