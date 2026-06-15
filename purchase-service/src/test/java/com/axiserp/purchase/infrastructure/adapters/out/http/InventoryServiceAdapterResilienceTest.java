package com.axiserp.purchase.infrastructure.adapters.out.http;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class InventoryServiceAdapterResilienceTest {

    private RestClient.Builder builder;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.ResponseSpec responseSpec;
    private InventoryServiceAdapter adapter;

    private final UUID productId = UUID.randomUUID();
    private final UUID purchaseId = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        builder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        adapter = new InventoryServiceAdapter(builder, "http://inventory:8083", "test-key");
    }

    @Test
    void registerEntry_success_whenInventoryResponds() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertDoesNotThrow(() -> adapter.registerEntry(productId, 10, "PURCHASE", referenceId, "test"));
    }

    @Test
    void reverseMovements_success_whenInventoryResponds() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertDoesNotThrow(() -> adapter.reverseMovements(purchaseId));
    }

    @Test
    void registerEntry_makesCorrectHttpCall() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        adapter.registerEntry(productId, 10, "PURCHASE", referenceId, "test");

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(anyString(), any(Object[].class));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void reverseMovements_makesCorrectHttpCall() {
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        adapter.reverseMovements(purchaseId);

        verify(restClient).post();
        verify(requestBodyUriSpec).uri(anyString(), any(Object[].class));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void registerEntry_hasRetryableAnnotation() throws Exception {
        var method = InventoryServiceAdapter.class.getMethod(
                "registerEntry", UUID.class, int.class, String.class, UUID.class, String.class);
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class));
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
    }

    @Test
    void reverseMovements_hasResilienceAnnotations() throws Exception {
        var method = InventoryServiceAdapter.class.getMethod("reverseMovements", UUID.class);
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.retry.annotation.Retry.class));
        assertTrue(method.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class));
    }
}
