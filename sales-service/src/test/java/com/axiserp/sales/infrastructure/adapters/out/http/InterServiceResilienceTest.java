package com.axiserp.sales.infrastructure.adapters.out.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.axiserp.sales.domain.exception.InsufficientStockException;

@ExtendWith(MockitoExtension.class)
@Tag("inter-service")
class InterServiceResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private CatalogServiceAdapter catalogAdapter;
    private InventoryServiceAdapter inventoryAdapter;

    private final UUID productId = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        catalogAdapter = new CatalogServiceAdapter(restTemplate);
        ReflectionTestUtils.setField(catalogAdapter, "catalogServiceUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(catalogAdapter, "internalApiKey", "test-api-key");
        inventoryAdapter = new InventoryServiceAdapter(restTemplate);
        ReflectionTestUtils.setField(inventoryAdapter, "inventoryServiceUrl", "http://localhost:8083");
        ReflectionTestUtils.setField(inventoryAdapter, "internalApiKey", "test-api-key");
    }

    @Test
    void catalogDown_productExists_returnsFalse() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Catalog service down"));

        boolean result = catalogAdapter.productExists(productId);

        assertThat(result).as("Should return false when catalog is down").isFalse();
    }

    @Test
    void catalogDown_findProductSummary_returnsNull() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Catalog service down"));

        var result = catalogAdapter.findProductSummary(productId);

        assertThat(result).as("Should return null when catalog is down").isNull();
    }

    @Test
    void inventoryDown_checkAndExit_throwsInsufficientStock() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Inventory service down"));

        assertThatThrownBy(() -> inventoryAdapter.checkAndExit(productId, 5, "SALE", referenceId, "test"))
                .as("Should throw InsufficientStockException when inventory is down without CB")
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void inventoryDown_registerReturn_throwsRuntimeException() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Inventory service down"));

        assertThatThrownBy(() -> inventoryAdapter.registerReturn(productId, 5, "RETURN", referenceId, "test"))
                .as("Should throw RuntimeException when inventory is down without CB")
                .isInstanceOf(RuntimeException.class);
    }
}
