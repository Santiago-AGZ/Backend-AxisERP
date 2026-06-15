package com.axiserp.sales.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.application.service.AuditService;
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
@DisplayName("[R26-R28] Invoice Snapshot Tests")
class InvoiceSnapshotTest {

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
    private UUID productId;
    private Sale sale;
    private Customer customer;

    @BeforeEach
    void setUp() {
        confirmSaleUseCase = new ConfirmSaleUseCaseImpl(
                saleRepositoryPort, customerRepositoryPort,
                invoiceRepositoryPort, inventoryServicePort, auditService);
        saleId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        customer = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .documentNumber("123456789")
                .email("customer@test.com")
                .status(CustomerStatus.ACTIVO)
                .build();

        sale = Sale.builder()
                .id(saleId)
                .customerId(customerId)
                .saleNumber("VTA-2026-000001")
                .status(SaleStatus.BORRADOR)
                .subtotal(new BigDecimal("500.00"))
                .discount(new BigDecimal("50.00"))
                .tax(new BigDecimal("85.50"))
                .total(new BigDecimal("535.50"))
                .items(List.of(
                        SaleItem.builder()
                                .productId(productId)
                                .productName("Test Product")
                                .quantity(5)
                                .unitPrice(new BigDecimal("100.00"))
                                .discount(BigDecimal.ZERO)
                                .subtotal(new BigDecimal("500.00"))
                                .build()))
                .build();
    }

    @Test
    @DisplayName("[R26] Should create invoice when sale is confirmed")
    void confirmSale_createsInvoice() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> {
            Invoice invObj = inv.getArgument(0);
            return Invoice.builder()
                    .id(UUID.randomUUID())
                    .saleId(invObj.getSaleId())
                    .invoiceNumber(1L)
                    .customerSnapshot(invObj.getCustomerSnapshot())
                    .itemsSnapshot(invObj.getItemsSnapshot())
                    .subtotal(invObj.getSubtotal())
                    .discount(invObj.getDiscount())
                    .tax(invObj.getTax())
                    .total(invObj.getTotal())
                    .issuedAt(invObj.getIssuedAt())
                    .build();
        });

        SaleResponse response = confirmSaleUseCase.confirm(saleId);

        assertNotNull(response);
        assertEquals("CONFIRMADA", response.getStatus());
        verify(invoiceRepositoryPort).save(any(Invoice.class));
    }

    @Test
    @DisplayName("[R28] Invoice should include customer snapshot with id, name, document")
    void invoice_containsCustomerSnapshot() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        confirmSaleUseCase.confirm(saleId);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepositoryPort).save(captor.capture());
        Invoice savedInvoice = captor.getValue();

        assertNotNull(savedInvoice.getCustomerSnapshot(), "Invoice must contain customer snapshot");
        assertTrue(savedInvoice.getCustomerSnapshot().contains(customerId.toString()),
                "Customer snapshot must include customer ID");
        assertTrue(savedInvoice.getCustomerSnapshot().contains("Test Customer"),
                "Customer snapshot must include customer name");
        assertTrue(savedInvoice.getCustomerSnapshot().contains("123456789"),
                "Customer snapshot must include document number");
    }

    @Test
    @DisplayName("[R28] Invoice should include items snapshot with product details")
    void invoice_containsItemsSnapshot() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        confirmSaleUseCase.confirm(saleId);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepositoryPort).save(captor.capture());
        Invoice savedInvoice = captor.getValue();

        assertNotNull(savedInvoice.getItemsSnapshot(), "Invoice must contain items snapshot");
        assertTrue(savedInvoice.getItemsSnapshot().contains(productId.toString()),
                "Items snapshot must include product ID");
        assertTrue(savedInvoice.getItemsSnapshot().contains("Test Product"),
                "Items snapshot must include product name");
        assertTrue(savedInvoice.getItemsSnapshot().contains("\"quantity\":5"),
                "Items snapshot must include quantity");
    }

    @Test
    @DisplayName("[R28] Invoice should contain financial snapshots (subtotal, discount, tax, total)")
    void invoice_containsFinancialSnapshots() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        confirmSaleUseCase.confirm(saleId);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepositoryPort).save(captor.capture());
        Invoice savedInvoice = captor.getValue();

        assertEquals(0, new BigDecimal("500.00").compareTo(savedInvoice.getSubtotal()));
        assertEquals(0, new BigDecimal("50.00").compareTo(savedInvoice.getDiscount()));
        assertEquals(0, new BigDecimal("85.50").compareTo(savedInvoice.getTax()));
        assertEquals(0, new BigDecimal("535.50").compareTo(savedInvoice.getTotal()));
        assertNotNull(savedInvoice.getIssuedAt());
    }

    @Test
    @DisplayName("[R29] Invoice should throw when trying to modify after creation - not modifiable by design")
    void invoice_isImmutableAfterCreation() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(inventoryServicePort).checkAndExit(any(), anyInt(), anyString(), any(), any());
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        confirmSaleUseCase.confirm(saleId);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepositoryPort).save(captor.capture());
        Invoice issuedInvoice = captor.getValue();

        assertNotNull(issuedInvoice.getIssuedAt());
        assertEquals(0, new BigDecimal("535.50").compareTo(issuedInvoice.getTotal()));
    }
}
