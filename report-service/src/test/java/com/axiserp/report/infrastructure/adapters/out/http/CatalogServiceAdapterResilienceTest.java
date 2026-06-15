package com.axiserp.report.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
class CatalogServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private CatalogServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CatalogServiceAdapter(restTemplate);
    }

    @Test
    void fetchProducts_returnsBody_whenCatalogResponds() {
        var node = mock(JsonNode.class);
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(node));

        JsonNode result = adapter.fetchProducts(null, null, false, 1, 10);

        assertThat(result).isSameAs(node);
    }

    @Test
    void fetchProducts_throwsException_whenCatalogTimesOut() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.fetchProducts(null, null, false, 1, 10))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    void fetchCategories_throwsException_whenCatalogDown() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Catalog unavailable"));

        assertThatThrownBy(() -> adapter.fetchCategories(null, false, 1, 10))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Disabled("CRITICAL: No try/catch, no fallback, no CircuitBreaker — exceptions propagate to controller")
    void fetchProducts_risk_noErrorHandling() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.fetchProducts(null, null, false, 1, 10))
                .isInstanceOf(ResourceAccessException.class);
    }
}
