package com.axiserp.sales.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.axiserp.sales.domain.exception.InsufficientStockException;

@ExtendWith(MockitoExtension.class)
class InventoryServiceAdapterResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private InventoryServiceAdapter adapter;

    private final UUID productId = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new InventoryServiceAdapter(restTemplate);
        ReflectionTestUtils.setField(adapter, "inventoryServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(adapter, "internalApiKey", "test-api-key");
    }

    @Test
    void checkAndExit_success_whenInventoryResponds() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        adapter.checkAndExit(productId, 5, "SALE", referenceId, "test");
    }

    @Test
    void checkAndExit_throwsInsufficientStock_onConflict() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> adapter.checkAndExit(productId, 5, "SALE", referenceId, "test"))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void checkAndExit_throwsInsufficientStock_onTimeout() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.checkAndExit(productId, 5, "SALE", referenceId, "test"))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void checkAndExit_throwsInsufficientStock_onServerError() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> adapter.checkAndExit(productId, 5, "SALE", referenceId, "test"))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void registerReturn_throwsRuntimeException_onTimeout() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> adapter.registerReturn(productId, 5, "RETURN", referenceId, "test"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void registerReturn_throwsRuntimeException_onServerError() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> adapter.registerReturn(productId, 5, "RETURN", referenceId, "test"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Disabled("Risk: No CircuitBreaker or Retry — every failure hits the network directly")
    void checkAndExit_risk_noResiliencePatterns() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Read timed out"));

        for (int i = 0; i < 10; i++) {
            assertThatThrownBy(() -> adapter.checkAndExit(productId, 5, "SALE", referenceId, "test"))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }
}
