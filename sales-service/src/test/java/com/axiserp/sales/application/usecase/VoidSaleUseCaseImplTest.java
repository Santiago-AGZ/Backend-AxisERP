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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.output.InventoryServicePort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoidSaleUseCaseImpl")
class VoidSaleUseCaseImplTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;
    @Mock
    private InventoryServicePort inventoryServicePort;
    @Mock
    private AuditService auditService;

    private VoidSaleUseCaseImpl voidSaleUseCase;
    private UUID saleId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        voidSaleUseCase = new VoidSaleUseCaseImpl(saleRepositoryPort, inventoryServicePort, auditService);
        saleId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should void CONFIRMADA sale and restore stock")
    void voidSale_confirmada_restoresStock() {
        Sale sale = Sale.builder()
                .id(saleId)
                .status(SaleStatus.CONFIRMADA)
                .items(List.of(SaleItem.builder().productId(productId).quantity(5).build()))
                .build();

        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = voidSaleUseCase.voidSale(saleId);

        assertEquals(SaleStatus.ANULADA.name(), response.getStatus());
        verify(inventoryServicePort).registerReturn(productId, 5, "DEVOLUCION_VENTA", saleId, null);
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException for PAGADA")
    void voidSale_pagada_throws() {
        Sale sale = Sale.builder()
                .id(saleId)
                .status(SaleStatus.PAGADA)
                .items(List.of(SaleItem.builder().productId(productId).quantity(3).build()))
                .build();

        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        assertThrows(SaleNotModifiableException.class, () -> voidSaleUseCase.voidSale(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException when sale not found")
    void voidSale_notFound_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.empty());
        assertThrows(SaleNotFoundException.class, () -> voidSaleUseCase.voidSale(saleId));
    }

    @Test
    @DisplayName("Should throw SaleNotModifiableException for BORRADOR")
    void voidSale_borrador_throws() {
        Sale sale = Sale.builder()
                .id(saleId)
                .status(SaleStatus.BORRADOR)
                .items(List.of(SaleItem.builder().build()))
                .build();

        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        assertThrows(SaleNotModifiableException.class, () -> voidSaleUseCase.voidSale(saleId));
    }
}
