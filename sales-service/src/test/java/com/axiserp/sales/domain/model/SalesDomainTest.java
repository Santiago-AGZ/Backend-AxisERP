package com.axiserp.sales.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Sale Domain Model")
class SaleDomainTest {

    @Test
    @DisplayName("Should create sale with BORRADOR status")
    void createSale_borrador() {
        Sale sale = Sale.builder()
                .id(UUID.randomUUID())
                .status(SaleStatus.BORRADOR)
                .items(List.of())
                .build();

        assertEquals(SaleStatus.BORRADOR, sale.getStatus());
        assertTrue(sale.isModifiable());
    }

    @Test
    @DisplayName("isModifiable should return false for ANULADA")
    void isModifiable_anulada() {
        Sale sale = Sale.builder().status(SaleStatus.ANULADA).build();
        assertFalse(sale.isModifiable());
    }

    @Test
    @DisplayName("isModifiable should return false for PAGADA")
    void isModifiable_pagada() {
        Sale sale = Sale.builder().status(SaleStatus.PAGADA).build();
        assertFalse(sale.isModifiable());
    }

    @Test
    @DisplayName("isModifiable should return true for BORRADOR")
    void isModifiable_borrador() {
        assertTrue(Sale.builder().status(SaleStatus.BORRADOR).build().isModifiable());
    }

    @Test
    @DisplayName("isModifiable should return true for PENDIENTE")
    void isModifiable_pendiente() {
        assertTrue(Sale.builder().status(SaleStatus.PENDIENTE).build().isModifiable());
    }

    @Test
    @DisplayName("isModifiable should return true for CONFIRMADA")
    void isModifiable_confirmada() {
        assertTrue(Sale.builder().status(SaleStatus.CONFIRMADA).build().isModifiable());
    }
}

@DisplayName("SaleItem Domain Model")
class SaleItemDomainTest {

    @Test
    @DisplayName("Should create sale item with subtotal")
    void createSaleItem() {
        SaleItem item = SaleItem.builder()
                .productId(UUID.randomUUID())
                .productName("Test Product")
                .quantity(5)
                .unitPrice(new BigDecimal("100.00"))
                .discount(BigDecimal.ZERO)
                .subtotal(new BigDecimal("500.00"))
                .build();

        assertEquals("Test Product", item.getProductName());
        assertEquals(5, item.getQuantity());
        assertEquals(0, item.getSubtotal().compareTo(new BigDecimal("500.00")));
    }
}

@DisplayName("Customer Domain Model")
class CustomerDomainTest {

    @Test
    @DisplayName("isActive should return true for ACTIVO")
    void isActive_activo() {
        Customer c = Customer.builder().status(CustomerStatus.ACTIVO).build();
        assertTrue(c.isActive());
        assertFalse(c.isDeleted());
    }

    @Test
    @DisplayName("isActive should return false for INACTIVO, isDeleted false")
    void isActive_inactivo() {
        Customer c = Customer.builder().status(CustomerStatus.INACTIVO).build();
        assertFalse(c.isActive());
        assertFalse(c.isDeleted());
    }

    @Test
    @DisplayName("isDeleted should return true for ELIMINADO")
    void isDeleted_eliminado() {
        Customer c = Customer.builder().status(CustomerStatus.ELIMINADO).build();
        assertTrue(c.isDeleted());
        assertFalse(c.isActive());
    }
}

@DisplayName("Invoice Domain Model")
class InvoiceDomainTest {

    @Test
    @DisplayName("Should create invoice")
    void createInvoice() {
        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .saleId(UUID.randomUUID())
                .invoiceNumber(1L)
                .total(new BigDecimal("595.00"))
                .build();

        assertNotNull(invoice.getId());
        assertEquals(1L, invoice.getInvoiceNumber());
        assertEquals(0, invoice.getTotal().compareTo(new BigDecimal("595.00")));
    }
}
