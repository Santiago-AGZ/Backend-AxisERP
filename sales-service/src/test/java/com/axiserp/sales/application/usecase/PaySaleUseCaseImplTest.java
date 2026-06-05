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

import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaySaleUseCaseImpl")
class PaySaleUseCaseImplTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private PaySaleUseCaseImpl paySaleUseCase;
    private UUID saleId;
    private Sale sale;

    @BeforeEach
    void setUp() {
        paySaleUseCase = new PaySaleUseCaseImpl(saleRepositoryPort);
        saleId = UUID.randomUUID();

        sale = Sale.builder()
                .id(saleId)
                .status(SaleStatus.CONFIRMADA)
                .items(List.of(SaleItem.builder().build()))
                .build();
    }

    @Test
    @DisplayName("Should pay sale from CONFIRMADA")
    void pay_confirmada_success() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = paySaleUseCase.pay(saleId);

        assertEquals(SaleStatus.PAGADA.name(), response.getStatus());
        verify(saleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException when sale not found")
    void pay_notFound_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.empty());
        assertThrows(SaleNotFoundException.class, () -> paySaleUseCase.pay(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException from BORRADOR")
    void pay_borrador_throws() {
        sale.setStatus(SaleStatus.BORRADOR);
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        assertThrows(SaleNotModifiableException.class, () -> paySaleUseCase.pay(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException from ANULADA")
    void pay_anulada_throws() {
        sale.setStatus(SaleStatus.ANULADA);
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        assertThrows(SaleNotModifiableException.class, () -> paySaleUseCase.pay(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException from PAGADA")
    void pay_pagada_throws() {
        sale.setStatus(SaleStatus.PAGADA);
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        assertThrows(SaleNotModifiableException.class, () -> paySaleUseCase.pay(saleId));
    }
}
