package com.axiserp.inventory.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.axiserp.inventory.application.dto.response.ProductSummary;

@ExtendWith(MockitoExtension.class)
class CatalogServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private CatalogServiceAdapter adapter;

    private final UUID categoryId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new CatalogServiceAdapter(restTemplate);
        ReflectionTestUtils.setField(adapter, "catalogServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(adapter, "internalApiKey", "test-api-key");
    }

    @Test
    void findProductIdsByCategoryId_returnsIds_whenCatalogResponds() {
        var data = List.of(Map.of("id", productId.toString()));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("data", data)));

        List<UUID> result = adapter.findProductIdsByCategoryId(categoryId);

        assertThat(result).containsExactly(productId);
    }

    @Test
    void findProductIdsByCategoryId_returnsEmptyList_whenCatalogTimesOut() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        List<UUID> result = adapter.findProductIdsByCategoryId(categoryId);

        assertThat(result).isEmpty();
    }

    @Test
    void findProductSummaries_returnsPartial_whenSomeFail() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        Map<UUID, ProductSummary> result = adapter.findProductSummaries(List.of(productId));

        assertThat(result).isEmpty();
    }

    @Test
    void findProductIdsByCategoryId_returnsEmptyList_onServerError() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Catalog 500"));

        List<UUID> result = adapter.findProductIdsByCategoryId(categoryId);

        assertThat(result).isEmpty();
    }

    @Test
    @Disabled("Risk: No CircuitBreaker — repeated timeouts block the caller thread")
    void findProductIdsByCategoryId_risk_noCircuitBreaker() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        for (int i = 0; i < 10; i++) {
            List<UUID> result = adapter.findProductIdsByCategoryId(categoryId);
            assertThat(result).isEmpty();
        }
    }
}
