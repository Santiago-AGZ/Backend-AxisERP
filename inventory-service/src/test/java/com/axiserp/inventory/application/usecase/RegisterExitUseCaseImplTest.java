package com.axiserp.inventory.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.service.AuditService;
import com.axiserp.inventory.domain.exception.InsufficientStockException;
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.exception.NegativeQuantityException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterExitUseCaseImpl")
class RegisterExitUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RegisterExitUseCaseImpl registerExitUseCase;

    private UUID productId;
    private UUID createdBy;
    private Inventory inventory;
    private InventoryMovement savedMovement;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(20)
                .build();
        savedMovement = InventoryMovement.builder()
                .id(UUID.randomUUID())
                .inventoryId(inventory.getId())
                .productId(productId)
                .movementType(MovementType.SALIDA)
                .quantity(5)
                .previousStock(20)
                .newStock(15)
                .createdBy(createdBy)
                .build();
    }

    @Test
    @DisplayName("Should register exit successfully")
    void registerExit_success() {
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class))).thenReturn(savedMovement);

        MovementResponse response = registerExitUseCase.registerExit(productId, 5, "VENTA", UUID.randomUUID(), "Salida por venta", createdBy);

        assertNotNull(response);
        assertEquals(MovementType.SALIDA.name(), response.getMovementType());
        assertEquals(5, response.getQuantity());
        assertEquals(20, response.getPreviousStock());
        assertEquals(15, response.getNewStock());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(inventoryRepositoryPort).saveMovement(any(InventoryMovement.class));
        verify(auditService).logStockExit(eq(productId), eq(createdBy), eq(5), eq(20), eq(15));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException when quantity is zero")
    void registerExit_zeroQuantity_throws() {
        assertThrows(NegativeQuantityException.class,
                () -> registerExitUseCase.registerExit(productId, 0, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when not enough stock")
    void registerExit_insufficientStock_throws() {
        Inventory lowInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(3)
                .build();

        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(lowInventory));

        assertThrows(InsufficientStockException.class,
                () -> registerExitUseCase.registerExit(productId, 10, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when product not found")
    void registerExit_productNotFound_throws() {
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> registerExitUseCase.registerExit(productId, 5, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }
}
