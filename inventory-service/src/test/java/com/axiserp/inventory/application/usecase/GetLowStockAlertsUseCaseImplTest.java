package com.axiserp.inventory.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.application.dto.response.ProductSummary;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.PageResult;
import com.axiserp.inventory.ports.output.CatalogServicePort;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLowStockAlertsUseCaseImpl")
class GetLowStockAlertsUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private CatalogServicePort catalogServicePort;

    @InjectMocks
    private GetLowStockAlertsUseCaseImpl getLowStockAlertsUseCase;

    @Test
    @DisplayName("Should return paged low stock alerts")
    void execute_success() {
        UUID productId = UUID.randomUUID();
        Inventory lowStockInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(3)
                .minStock(10)
                .build();
        ProductSummary summary = ProductSummary.builder()
                .id(productId)
                .name("Low Stock Product")
                .codigo("P-001")
                .build();

        when(inventoryRepositoryPort.findLowStock(0, 10)).thenReturn(List.of(lowStockInventory));
        when(inventoryRepositoryPort.countLowStock()).thenReturn(1L);
        when(catalogServicePort.findProductSummaries(anyList()))
                .thenReturn(Map.of(productId, summary));

        PageResult<ProductInventoryResponse> result = getLowStockAlertsUseCase.execute(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotal());
        ProductInventoryResponse response = result.getContent().getFirst();
        assertEquals(productId, response.getProductId());
        assertEquals("Low Stock Product", response.getProductName());
        assertEquals(3, response.getCurrentStock());
        assertTrue(response.isLowStock());
    }

    @Test
    @DisplayName("Should return empty page when no low stock items")
    void execute_empty() {
        when(inventoryRepositoryPort.findLowStock(0, 10)).thenReturn(List.of());
        when(inventoryRepositoryPort.countLowStock()).thenReturn(0L);
        when(catalogServicePort.findProductSummaries(anyList())).thenReturn(Map.of());

        PageResult<ProductInventoryResponse> result = getLowStockAlertsUseCase.execute(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when page is negative")
    void execute_negativePage_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> getLowStockAlertsUseCase.execute(-1, 10));
        verify(inventoryRepositoryPort, never()).findLowStock(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should clamp size to 1-100 range")
    void execute_clampsSize() {
        when(inventoryRepositoryPort.findLowStock(0, 1)).thenReturn(List.of());
        when(inventoryRepositoryPort.countLowStock()).thenReturn(0L);
        when(catalogServicePort.findProductSummaries(anyList())).thenReturn(Map.of());

        getLowStockAlertsUseCase.execute(0, -5);
        verify(inventoryRepositoryPort).findLowStock(0, 1);

        getLowStockAlertsUseCase.execute(0, 1000);
        verify(inventoryRepositoryPort).findLowStock(0, 100);
    }
}
