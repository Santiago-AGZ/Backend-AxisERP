package com.axiserp.purchase.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.output.InventoryServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPurchaseUseCaseImpl")
class CancelPurchaseUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;
    @Mock
    private InventoryServicePort inventoryServicePort;

    private CancelPurchaseUseCaseImpl cancelPurchaseUseCase;

    private UUID purchaseId;

    @BeforeEach
    void setUp() {
        cancelPurchaseUseCase = new CancelPurchaseUseCaseImpl(purchaseRepositoryPort, inventoryServicePort);
        purchaseId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should cancel purchase with received items and reverse inventory")
    void execute_withReceivedItems_reversesInventory() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.PENDIENTE)
                .items(List.of(
                        PurchaseItem.builder()
                                .id(UUID.randomUUID())
                                .productId(UUID.randomUUID())
                                .quantity(10)
                                .receivedQuantity(5)
                                .build()))
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = cancelPurchaseUseCase.execute(purchaseId);

        assertEquals(PurchaseStatus.CANCELADA, response.getStatus());
        verify(inventoryServicePort).reverseMovements(purchaseId);
    }

    @Test
    @DisplayName("Should cancel purchase without received items without reversing inventory")
    void execute_withoutReceivedItems_noInventoryReversal() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.PENDIENTE)
                .items(List.of(
                        PurchaseItem.builder()
                                .id(UUID.randomUUID())
                                .productId(UUID.randomUUID())
                                .quantity(10)
                                .receivedQuantity(0)
                                .build()))
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = cancelPurchaseUseCase.execute(purchaseId);

        assertEquals(PurchaseStatus.CANCELADA, response.getStatus());
        verify(inventoryServicePort, never()).reverseMovements(any());
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when purchase not found")
    void execute_purchaseNotFound_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class,
                () -> cancelPurchaseUseCase.execute(purchaseId));
    }

    @Test
    @DisplayName("Should throw PurchaseNotModifiableException when already cancelled")
    void execute_alreadyCancelled_throws() {
        Purchase cancelled = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.CANCELADA)
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(cancelled));

        assertThrows(PurchaseNotModifiableException.class,
                () -> cancelPurchaseUseCase.execute(purchaseId));
    }

    @Test
    @DisplayName("Should throw PurchaseNotModifiableException when already paid")
    void execute_alreadyPaid_throws() {
        Purchase paid = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.PAGADA)
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(paid));

        assertThrows(PurchaseNotModifiableException.class,
                () -> cancelPurchaseUseCase.execute(purchaseId));
    }
}
