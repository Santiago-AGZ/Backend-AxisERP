package com.axiserp.purchase.infrastructure.adapters.out.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.axiserp.purchase.infrastructure.adapters.out.catalog.CatalogServiceAdapter;

@Tag("inter-service")
class InterServiceResilienceTest {

    private RestClient.Builder builder;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;

    private CatalogServiceAdapter catalogAdapter;
    private InventoryServiceAdapter inventoryAdapter;

    private final UUID productId = UUID.randomUUID();
    private final UUID purchaseId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        builder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        catalogAdapter = new CatalogServiceAdapter(builder, "http://catalog:8082", "test-key");
        inventoryAdapter = new InventoryServiceAdapter(builder, "http://inventory:8083", "test-key");
    }

    @Test
    void purchaseService_hasResilienceOnBothAdapters() {
        assertNotNull(catalogAdapter);
        assertNotNull(inventoryAdapter);
    }

    @Test
    void catalogAdapter_resilienceAnnotationsPresent() throws Exception {
        var method = CatalogServiceAdapter.class.getMethod("productExists", UUID.class);
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class),
                "CatalogServiceAdapter should have @CircuitBreaker");
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class),
                "CatalogServiceAdapter should have @Retry");
    }

    @Test
    void inventoryAdapter_resilienceAnnotationsPresent() throws Exception {
        var registerMethod = InventoryServiceAdapter.class.getMethod(
                "registerEntry", UUID.class, int.class, String.class, UUID.class, String.class);
        var reverseMethod = InventoryServiceAdapter.class.getMethod("reverseMovements", UUID.class);

        assertTrue(registerMethod.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
        assertTrue(registerMethod.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class));
        assertTrue(reverseMethod.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
        assertTrue(reverseMethod.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class));
    }

    @Test
    void purchaseService_bothAdapters_workOnSuccess() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertTrue(catalogAdapter.productExists(productId));
        assertDoesNotThrow(() -> inventoryAdapter.registerEntry(productId, 10, "PURCHASE", purchaseId, "test"));
    }

    private static void assertDoesNotThrow(org.junit.jupiter.api.function.Executable exec) {
        try {
            exec.execute();
        } catch (Throwable e) {
            throw new AssertionError("Expected no exception", e);
        }
    }
}
