package com.axiserp.purchase.infrastructure.adapters.out.catalog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

class CatalogServiceAdapterResilienceTest {

    private RestClient.Builder builder;
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;
    private CatalogServiceAdapter adapter;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        builder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        adapter = new CatalogServiceAdapter(builder, "http://catalog:8082", "test-key");
    }

    @Test
    void productExists_returnsTrue_whenCatalogResponds() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        boolean result = adapter.productExists(productId);

        assertTrue(result);
    }

    @Test
    void productExists_returnsFalse_whenCatalogReturns404() {
        when(responseSpec.toBodilessEntity())
                .thenThrow(HttpClientErrorException.NotFound.class);

        boolean result = adapter.productExists(productId);

        assertFalse(result);
    }

    @Test
    void productExists_makesCorrectHttpCall() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        adapter.productExists(productId);

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(contains("/api/v1/productos/"), eq(productId));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void productExists_hasRetryableAnnotation() throws Exception {
        var method = CatalogServiceAdapter.class.getMethod("productExists", UUID.class);
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class));
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
    }

    @Test
    void productExists_annotationReferencesCorrectCB() throws Exception {
        var method = CatalogServiceAdapter.class.getMethod("productExists", UUID.class);
        var cb = method.getAnnotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class);
        assertTrue(cb.name().equals("catalogService"));
    }
}
