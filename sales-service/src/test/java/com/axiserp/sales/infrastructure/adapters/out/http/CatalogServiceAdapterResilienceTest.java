package com.axiserp.sales.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.axiserp.sales.application.dto.response.ProductSummary;

@ExtendWith(MockitoExtension.class)
class CatalogServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private CatalogServiceAdapter adapter;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new CatalogServiceAdapter(restTemplate);
        ReflectionTestUtils.setField(adapter, "catalogServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(adapter, "internalApiKey", "test-api-key");
    }

    @Test
    void productExists_returnsTrue_whenCatalogResponds() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("data", Map.of("id", productId.toString(), "name", "Test"))));

        boolean result = adapter.productExists(productId);

        assertThat(result).isTrue();
    }

    @Test
    void productExists_returnsFalse_whenCatalogReturns404() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        boolean result = adapter.productExists(productId);

        assertThat(result).isFalse();
    }

    @Test
    void productExists_fallbackFalse_whenCatalogTimesOut() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        boolean result = adapter.productExists(productId);

        assertThat(result).isFalse();
    }

    @Test
    void findProductSummary_returnsNull_whenCatalogTimesOut() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        ProductSummary result = adapter.findProductSummary(productId);

        assertThat(result).isNull();
    }

    @Test
    @Disabled("Risk: No CircuitBreaker — repeated failures cascade to caller without fast-fail")
    void productExists_risk_noCircuitBreakerForCascadingFailures() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        for (int i = 0; i < 10; i++) {
            boolean result = adapter.productExists(productId);
            assertThat(result).isFalse();
        }
    }
}
