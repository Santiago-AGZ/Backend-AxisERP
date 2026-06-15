package com.axiserp.report.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
class SalesServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private SalesServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SalesServiceAdapter(restTemplate);
    }

    @Test
    void fetchSales_returnsBody_whenSalesResponds() {
        var node = mock(JsonNode.class);
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(node));

        JsonNode result = adapter.fetchSales(LocalDate.now(), LocalDate.now(), "COMPLETADA", null, null, 1, 10);

        assertThat(result).isSameAs(node);
    }

    @Test
    void fetchSales_throwsException_whenSalesTimesOut() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.fetchSales(LocalDate.now(), LocalDate.now(), "COMPLETADA", null, null, 1, 10))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    void fetchCustomers_throwsException_whenSalesDown() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Sales service unavailable"));

        assertThatThrownBy(() -> adapter.fetchCustomers("test", false, 1, 10))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void fetchCustomerById_throwsException_whenSalesDown() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.fetchCustomerById(UUID.randomUUID()))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    void fetchSalesByIds_throwsException_whenSalesDown() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> adapter.fetchSalesByIds("id1,id2"))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    @Disabled("CRITICAL: No try/catch, no fallback, no CircuitBreaker — exceptions propagate to controller")
    void fetchSales_risk_noErrorHandlingAtAll() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.fetchSales(LocalDate.now(), LocalDate.now(), "COMPLETADA", null, null, 1, 10))
                .isInstanceOf(ResourceAccessException.class);
    }
}
