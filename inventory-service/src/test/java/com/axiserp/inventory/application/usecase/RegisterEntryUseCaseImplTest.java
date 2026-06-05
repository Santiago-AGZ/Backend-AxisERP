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
import com.axiserp.inventory.domain.exception.InventoryNotFoundException;
import com.axiserp.inventory.domain.exception.NegativeQuantityException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterEntryUseCaseImpl")
class RegisterEntryUseCaseImplTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RegisterEntryUseCaseImpl registerEntryUseCase;

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
                .currentStock(10)
                .build();
        savedMovement = InventoryMovement.builder()
                .id(UUID.randomUUID())
                .inventoryId(inventory.getId())
                .productId(productId)
                .movementType(MovementType.ENTRADA)
                .quantity(5)
                .previousStock(10)
                .newStock(15)
                .createdBy(createdBy)
                .build();
    }

    @Test
    @DisplayName("Should register entry successfully")
    void registerEntry_success() {
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryRepositoryPort.saveMovement(any(InventoryMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MovementResponse response = registerEntryUseCase.registerEntry(productId, 5, "COMPRA", UUID.randomUUID(), "Entrada por compra", createdBy);

        assertNotNull(response);
        assertEquals(MovementType.ENTRADA.name(), response.getMovementType());
        assertEquals(5, response.getQuantity());
        assertEquals(10, response.getPreviousStock());
        assertEquals(15, response.getNewStock());
        assertEquals("Entrada por compra", response.getNotes());
        verify(inventoryRepositoryPort).save(any(Inventory.class));
        verify(inventoryRepositoryPort).saveMovement(any(InventoryMovement.class));
        verify(auditService).logStockEntry(eq(productId), eq(createdBy), eq(5), eq(10), eq(15));
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException when quantity is zero")
    void registerEntry_zeroQuantity_throws() {
        assertThrows(NegativeQuantityException.class,
                () -> registerEntryUseCase.registerEntry(productId, 0, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NegativeQuantityException when quantity is negative")
    void registerEntry_negativeQuantity_throws() {
        assertThrows(NegativeQuantityException.class,
                () -> registerEntryUseCase.registerEntry(productId, -1, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InventoryNotFoundException when product not found")
    void registerEntry_productNotFound_throws() {
        when(inventoryRepositoryPort.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> registerEntryUseCase.registerEntry(productId, 5, null, null, null, createdBy));
        verify(inventoryRepositoryPort, never()).save(any());
    }
}
