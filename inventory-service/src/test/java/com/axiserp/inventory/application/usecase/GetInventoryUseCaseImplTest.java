package com.axiserp.inventory.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInventoryUseCaseImpl")
class GetInventoryUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @InjectMocks
    private GetInventoryUseCaseImpl getInventoryUseCase;

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return inventory for existing product")
    void getByProductId_success() {
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(25)
                .minStock(5)
                .maxStock(100)
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse response = getInventoryUseCase.getByProductId(productId);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(25, response.getCurrentStock());
        assertEquals(5, response.getMinStock());
        assertEquals(100, response.getMaxStock());
        assertFalse(response.isLowStock());
        assertFalse(response.isDepleted());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when product not found")
    void getByProductId_notFound_throws() {
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> getInventoryUseCase.getByProductId(productId));
    }

    @Test
    @DisplayName("Should detect low stock and depleted state")
    void getByProductId_detectsLowStockAndDepleted() {
        Inventory lowInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(3)
                .minStock(10)
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(lowInventory));

        InventoryResponse response = getInventoryUseCase.getByProductId(productId);

        assertTrue(response.isLowStock());
        assertFalse(response.isDepleted());
    }

    @Test
    @DisplayName("Should detect depleted state")
    void getByProductId_detectsDepleted() {
        Inventory depletedInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(0)
                .minStock(10)
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(depletedInventory));

        InventoryResponse response = getInventoryUseCase.getByProductId(productId);

        assertFalse(response.isLowStock());
        assertTrue(response.isDepleted());
    }
}
