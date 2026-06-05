package com.axiserp.purchase.application.usecase;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.request.PurchaseItemRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.application.service.AuditService;
import com.axiserp.purchase.domain.exception.DuplicateProductInPurchaseException;
import com.axiserp.purchase.domain.exception.SupplierInactiveException;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.output.CatalogServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePurchaseUseCaseImpl")
class CreatePurchaseUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;
    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;
    @Mock
    private CatalogServicePort catalogServicePort;
    @Mock
    private AuditService auditService;

    private CreatePurchaseUseCaseImpl createPurchaseUseCase;

    private UUID supplierId;
    private UUID productId;
    private UUID createdBy;
    private CreatePurchaseRequest request;
    private Supplier activeSupplier;

    @BeforeEach
    void setUp() {
        createPurchaseUseCase = new CreatePurchaseUseCaseImpl(
                purchaseRepositoryPort, supplierRepositoryPort, catalogServicePort, auditService);

        supplierId = UUID.randomUUID();
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        activeSupplier = Supplier.builder()
                .id(supplierId)
                .codigo("PROV-000001")
                .name("Proveedor Test")
                .nit("123456789")
                .status(SupplierStatus.ACTIVO)
                .build();

        request = CreatePurchaseRequest.builder()
                .supplierId(supplierId)
                .items(List.of(
                        PurchaseItemRequest.builder()
                                .productId(productId)
                                .productName("Producto Test")
                                .quantity(5)
                                .unitPrice(new BigDecimal("100.00"))
                                .build()))
                .notes("Compra de prueba")
                .build();
    }

    @Test
    @DisplayName("Should create purchase successfully")
    void execute_success() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
        when(catalogServicePort.productExists(productId)).thenReturn(true);
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponse response = createPurchaseUseCase.execute(request, createdBy);

        assertNotNull(response);
        assertEquals(supplierId, response.getSupplierId());
        assertEquals(PurchaseStatus.BORRADOR, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(0, response.getSubtotal().compareTo(new BigDecimal("500.00")));
        assertEquals(0, response.getTax().compareTo(new BigDecimal("95.00")));
        assertEquals(0, response.getTotal().compareTo(new BigDecimal("595.00")));
        assertTrue(response.getPurchaseNumber().startsWith("PO-"));
        verify(purchaseRepositoryPort).save(any());
        verify(auditService).logCreate(eq("PURCHASE"), any(), eq(createdBy), anyString());
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when supplier does not exist")
    void execute_supplierNotFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> createPurchaseUseCase.execute(request, createdBy));
        verify(purchaseRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw SupplierInactiveException when supplier is inactive")
    void execute_supplierInactive_throws() {
        Supplier inactiveSupplier = Supplier.builder()
                .id(supplierId)
                .status(SupplierStatus.INACTIVO)
                .build();
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(inactiveSupplier));

        assertThrows(SupplierInactiveException.class,
                () -> createPurchaseUseCase.execute(request, createdBy));
        verify(purchaseRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateProductInPurchaseException for duplicate products")
    void execute_duplicateProduct_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
        when(catalogServicePort.productExists(productId)).thenReturn(true);

        CreatePurchaseRequest dupRequest = CreatePurchaseRequest.builder()
                .supplierId(supplierId)
                .items(List.of(
                        PurchaseItemRequest.builder()
                                .productId(productId)
                                .productName("Producto A")
                                .quantity(2)
                                .unitPrice(new BigDecimal("50.00"))
                                .build(),
                        PurchaseItemRequest.builder()
                                .productId(productId)
                                .productName("Producto A dup")
                                .quantity(3)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()))
                .build();

        assertThrows(DuplicateProductInPurchaseException.class,
                () -> createPurchaseUseCase.execute(dupRequest, createdBy));
        verify(purchaseRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when product does not exist")
    void execute_productNotFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
        when(catalogServicePort.productExists(productId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> createPurchaseUseCase.execute(request, createdBy));
        verify(purchaseRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate totals with multiple items")
    void execute_multipleItems_calculatesTotals() {
        UUID productId2 = UUID.randomUUID();
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
        when(catalogServicePort.productExists(any())).thenReturn(true);
        when(purchaseRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatePurchaseRequest multiRequest = CreatePurchaseRequest.builder()
                .supplierId(supplierId)
                .items(List.of(
                        PurchaseItemRequest.builder()
                                .productId(productId)
                                .productName("Producto A")
                                .quantity(2)
                                .unitPrice(new BigDecimal("100.00"))
                                .build(),
                        PurchaseItemRequest.builder()
                                .productId(productId2)
                                .productName("Producto B")
                                .quantity(3)
                                .unitPrice(new BigDecimal("50.00"))
                                .build()))
                .build();

        PurchaseResponse response = createPurchaseUseCase.execute(multiRequest, createdBy);

        assertEquals(0, response.getSubtotal().compareTo(new BigDecimal("350.00")));
        assertEquals(0, response.getTax().compareTo(new BigDecimal("66.50")));
        assertEquals(0, response.getTotal().compareTo(new BigDecimal("416.50")));
    }
}
