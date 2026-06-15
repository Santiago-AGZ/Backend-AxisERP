package com.axiserp.catalog.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SalesServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private SalesServiceAdapter adapter;

    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new SalesServiceAdapter(restTemplate);
    }

    @Test
    void hasPendingSales_returnsFalse_whenSalesRespondsEmpty() {
        var mapper = new ObjectMapper();
        var node = mapper.createObjectNode();
        node.set("data", mapper.createArrayNode());
        when(restTemplate.exchange(anyString(), any(), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(node));

        boolean result = adapter.hasPendingSales(productId);

        assertThat(result).isFalse();
    }

    @Test
    void hasPendingSales_fallbackFalse_whenSalesTimesOut() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        boolean result = adapter.hasPendingSales(productId);

        assertThat(result).isFalse();
    }

    @Test
    void hasPendingSales_returnsFalse_whenSalesReturnsError() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenThrow(new RuntimeException("Sales service 500"));

        boolean result = adapter.hasPendingSales(productId);

        assertThat(result).isFalse();
    }

    @Test
    @Disabled("Risk: No CircuitBreaker — each failure invokes a full HTTP call despite catch block")
    void hasPendingSales_risk_noCircuitBreaker() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(com.fasterxml.jackson.databind.JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        for (int i = 0; i < 10; i++) {
            boolean result = adapter.hasPendingSales(productId);
            assertThat(result).isFalse();
        }
    }
}
