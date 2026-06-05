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
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePurchaseStatusUseCaseImpl")
class UpdatePurchaseStatusUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;

    private UpdatePurchaseStatusUseCaseImpl updatePurchaseStatusUseCase;

    private UUID purchaseId;

    @BeforeEach
    void setUp() {
        updatePurchaseStatusUseCase = new UpdatePurchaseStatusUseCaseImpl(purchaseRepositoryPort);
        purchaseId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should update from BORRADOR to PENDIENTE")
    void execute_borradorToPendiente_success() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.BORRADOR)
                .items(List.of())
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.PENDIENTE);

        assertEquals(PurchaseStatus.PENDIENTE, response.getStatus());
    }

    @Test
    @DisplayName("Should update from RECIBIDA to PAGADA")
    void execute_recibidaToPagada_success() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.RECIBIDA)
                .items(List.of())
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.PAGADA);

        assertEquals(PurchaseStatus.PAGADA, response.getStatus());
    }

    @Test
    @DisplayName("Should allow cancel from any modifiable state")
    void execute_cancelFromBorrador_success() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.BORRADOR)
                .items(List.of())
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.CANCELADA);

        assertEquals(PurchaseStatus.CANCELADA, response.getStatus());
    }

    @Test
    @DisplayName("Should throw PurchaseNotModifiableException for invalid transition")
    void execute_invalidTransition_throws() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.BORRADOR)
                .items(List.of())
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThrows(PurchaseNotModifiableException.class,
                () -> updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.PAGADA));
    }

    @Test
    @DisplayName("Should throw PurchaseNotModifiableException when cancel from PAGADA")
    void execute_cancelFromPagada_throws() {
        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.PAGADA)
                .items(List.of())
                .build();

        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));

        assertThrows(PurchaseNotModifiableException.class,
                () -> updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.CANCELADA));
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when purchase not found")
    void execute_purchaseNotFound_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class,
                () -> updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.PENDIENTE));
    }
}
