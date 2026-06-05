package com.axiserp.sales.application.usecase;

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

import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.exception.InsufficientStockException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.domain.model.Invoice;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.InventoryServicePort;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmSaleUseCaseImpl")
class ConfirmSaleUseCaseImplTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;
    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;
    @Mock
    private InventoryServicePort inventoryServicePort;
    @Mock
    private AuditService auditService;

    private ConfirmSaleUseCaseImpl confirmSaleUseCase;
    private UUID saleId;
    private UUID customerId;
    private Sale borradorSale;

    @BeforeEach
    void setUp() {
        confirmSaleUseCase = new ConfirmSaleUseCaseImpl(saleRepositoryPort, customerRepositoryPort,
                invoiceRepositoryPort, inventoryServicePort, auditService);
        saleId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        borradorSale = Sale.builder()
                .id(saleId)
                .customerId(customerId)
                .status(SaleStatus.BORRADOR)
                .items(List.of(SaleItem.builder()
                        .productId(UUID.randomUUID())
                        .quantity(5)
                        .build()))
                .build();
    }

    @Test
    @DisplayName("Should confirm sale from BORRADOR")
    void confirm_borrador_success() {
        Customer customer = Customer.builder().id(customerId).name("Test").documentNumber("123").status(CustomerStatus.ACTIVO).build();

        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> {
            Invoice invObj = inv.getArgument(0);
            return Invoice.builder()
                    .id(UUID.randomUUID())
                    .saleId(invObj.getSaleId())
                    .invoiceNumber(1L)
                    .build();
        });

        var response = confirmSaleUseCase.confirm(saleId);

        assertEquals(SaleStatus.CONFIRMADA.name(), response.getStatus());
        verify(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        verify(invoiceRepositoryPort).save(any());
    }

    @Test
    @DisplayName("Should confirm sale from PENDIENTE")
    void confirm_pendiente_success() {
        borradorSale.setStatus(SaleStatus.PENDIENTE);
        Customer customer = Customer.builder().id(customerId).name("Test").documentNumber("123").status(CustomerStatus.ACTIVO).build();

        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> Invoice.builder().id(UUID.randomUUID()).invoiceNumber(1L).build());

        var response = confirmSaleUseCase.confirm(saleId);

        assertEquals(SaleStatus.CONFIRMADA.name(), response.getStatus());
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException when sale not found")
    void confirm_saleNotFound_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.empty());
        assertThrows(SaleNotFoundException.class, () -> confirmSaleUseCase.confirm(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException when sale is PAGADA")
    void confirm_pagada_throws() {
        borradorSale.setStatus(SaleStatus.PAGADA);
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        assertThrows(SaleNotModifiableException.class, () -> confirmSaleUseCase.confirm(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException when sale is ANULADA")
    void confirm_anulada_throws() {
        borradorSale.setStatus(SaleStatus.ANULADA);
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        assertThrows(SaleNotModifiableException.class, () -> confirmSaleUseCase.confirm(saleId));
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock insufficient")
    void confirm_insufficientStock_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        doThrow(new InsufficientStockException("Stock insuficiente"))
                .when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());

        assertThrows(InsufficientStockException.class, () -> confirmSaleUseCase.confirm(saleId));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer missing after confirm")
    void confirm_customerNotFound_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(borradorSale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> confirmSaleUseCase.confirm(saleId));
    }
}
