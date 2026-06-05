package com.axiserp.purchase.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.request.UpdateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateSupplierUseCaseImpl")
class DeactivateSupplierUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private DeactivateSupplierUseCaseImpl deactivateSupplierUseCase;
    private UUID supplierId;
    private Supplier activeSupplier;

    @BeforeEach
    void setUp() {
        deactivateSupplierUseCase = new DeactivateSupplierUseCaseImpl(supplierRepositoryPort);
        supplierId = UUID.randomUUID();

        activeSupplier = Supplier.builder()
                .id(supplierId)
                .codigo("PROV-000001")
                .name("Test")
                .status(SupplierStatus.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Should deactivate supplier")
    void execute_success() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
        when(supplierRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SupplierResponse response = deactivateSupplierUseCase.execute(supplierId);

        assertEquals(SupplierStatus.ELIMINADO, response.getStatus());
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when supplier not found")
    void execute_notFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> deactivateSupplierUseCase.execute(supplierId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactivateSupplierUseCaseImpl")
class ReactivateSupplierUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private ReactivateSupplierUseCaseImpl reactivateSupplierUseCase;
    private UUID supplierId;
    private Supplier inactiveSupplier;

    @BeforeEach
    void setUp() {
        reactivateSupplierUseCase = new ReactivateSupplierUseCaseImpl(supplierRepositoryPort);
        supplierId = UUID.randomUUID();

        inactiveSupplier = Supplier.builder()
                .id(supplierId)
                .codigo("PROV-000001")
                .name("Test")
                .status(SupplierStatus.ELIMINADO)
                .build();
    }

    @Test
    @DisplayName("Should reactivate supplier")
    void execute_success() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(inactiveSupplier));
        when(supplierRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SupplierResponse response = reactivateSupplierUseCase.execute(supplierId);

        assertEquals(SupplierStatus.ACTIVO, response.getStatus());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when already active")
    void execute_alreadyActive_throws() {
        Supplier activeSupplier = Supplier.builder()
                .id(supplierId)
                .status(SupplierStatus.ACTIVO)
                .build();
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(activeSupplier));

        assertThrows(IllegalStateException.class,
                () -> reactivateSupplierUseCase.execute(supplierId));
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when supplier not found")
    void execute_notFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> reactivateSupplierUseCase.execute(supplierId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSupplierUseCaseImpl")
class UpdateSupplierUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private UpdateSupplierUseCaseImpl updateSupplierUseCase;
    private UUID supplierId;

    @BeforeEach
    void setUp() {
        updateSupplierUseCase = new UpdateSupplierUseCaseImpl(supplierRepositoryPort);
        supplierId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should update supplier successfully")
    void execute_success() {
        Supplier existing = Supplier.builder()
                .id(supplierId)
                .codigo("PROV-000001")
                .name("Original Name")
                .status(SupplierStatus.ACTIVO)
                .build();

        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(existing));
        when(supplierRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                .name("Updated Name")
                .phone("555-0200")
                .email("updated@test.com")
                .address("New Address")
                .build();

        SupplierResponse response = updateSupplierUseCase.execute(supplierId, request);

        assertEquals("Updated Name", response.getName());
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when supplier not found")
    void execute_notFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.empty());

        UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                .name("Test")
                .build();

        assertThrows(SupplierNotFoundException.class,
                () -> updateSupplierUseCase.execute(supplierId, request));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSupplierUseCaseImpl")
class GetSupplierUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private GetSupplierUseCaseImpl getSupplierUseCase;
    private UUID supplierId;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        getSupplierUseCase = new GetSupplierUseCaseImpl(supplierRepositoryPort);
        supplierId = UUID.randomUUID();

        supplier = Supplier.builder()
                .id(supplierId)
                .codigo("PROV-000001")
                .name("Test Supplier")
                .status(SupplierStatus.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Should get supplier by id")
    void execute_byId_success() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.of(supplier));

        SupplierResponse response = getSupplierUseCase.execute(supplierId);

        assertEquals(supplierId, response.getId());
        assertEquals("Test Supplier", response.getName());
    }

    @Test
    @DisplayName("Should get supplier by codigo")
    void execute_byCodigo_success() {
        when(supplierRepositoryPort.findByCodigo("PROV-000001")).thenReturn(Optional.of(supplier));

        SupplierResponse response = getSupplierUseCase.executeByCodigo("PROV-000001");

        assertEquals("PROV-000001", response.getCodigo());
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when not found by id")
    void execute_byId_notFound_throws() {
        when(supplierRepositoryPort.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> getSupplierUseCase.execute(supplierId));
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when not found by codigo")
    void execute_byCodigo_notFound_throws() {
        when(supplierRepositoryPort.findByCodigo("PROV-999999")).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> getSupplierUseCase.executeByCodigo("PROV-999999"));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPurchaseUseCaseImpl")
class GetPurchaseUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;

    private GetPurchaseUseCaseImpl getPurchaseUseCase;
    private UUID purchaseId;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        getPurchaseUseCase = new GetPurchaseUseCaseImpl(purchaseRepositoryPort);
        purchaseId = UUID.randomUUID();

        purchase = Purchase.builder()
                .id(purchaseId)
                .status(PurchaseStatus.BORRADOR)
                .items(List.of())
                .build();
    }

    @Test
    @DisplayName("Should get purchase by id")
    void execute_success() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.of(purchase));

        var response = getPurchaseUseCase.execute(purchaseId);

        assertEquals(purchaseId, response.getId());
        assertEquals(PurchaseStatus.BORRADOR, response.getStatus());
    }

    @Test
    @DisplayName("Should throw PurchaseNotFoundException when not found")
    void execute_notFound_throws() {
        when(purchaseRepositoryPort.findById(purchaseId)).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class,
                () -> getPurchaseUseCase.execute(purchaseId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ListSuppliersUseCaseImpl")
class ListSuppliersUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private ListSuppliersUseCaseImpl listSuppliersUseCase;

    @BeforeEach
    void setUp() {
        listSuppliersUseCase = new ListSuppliersUseCaseImpl(supplierRepositoryPort);
    }

    @Test
    @DisplayName("Should list all active suppliers")
    void execute_all_success() {
        when(supplierRepositoryPort.findAllActive()).thenReturn(List.of(
                Supplier.builder().codigo("PROV-000001").name("A").status(SupplierStatus.ACTIVO).build(),
                Supplier.builder().codigo("PROV-000002").name("B").status(SupplierStatus.ACTIVO).build()));

        var response = listSuppliersUseCase.execute();

        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("Should list suppliers with search and pagination")
    void execute_withSearch_success() {
        when(supplierRepositoryPort.findAllActive(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        var response = listSuppliersUseCase.execute("test", 0, 10);

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Should return count of all suppliers")
    void countAll_success() {
        when(supplierRepositoryPort.countAllActive()).thenReturn(5L);

        assertEquals(5L, listSuppliersUseCase.countAll());
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPurchasesUseCaseImpl")
class ListPurchasesUseCaseImplTest {

    @Mock
    private PurchaseRepositoryPort purchaseRepositoryPort;

    private ListPurchasesUseCaseImpl listPurchasesUseCase;

    @BeforeEach
    void setUp() {
        listPurchasesUseCase = new ListPurchasesUseCaseImpl(purchaseRepositoryPort);
    }

    @Test
    @DisplayName("Should list all purchases")
    void execute_all_success() {
        when(purchaseRepositoryPort.findAll()).thenReturn(List.of(
                Purchase.builder().status(PurchaseStatus.BORRADOR).items(List.of()).build(),
                Purchase.builder().status(PurchaseStatus.PENDIENTE).items(List.of()).build()));

        var response = listPurchasesUseCase.execute();

        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("Should list purchases with filters")
    void execute_withFilters_success() {
        when(purchaseRepositoryPort.findAll(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        var response = listPurchasesUseCase.execute("search", "PENDIENTE", 0, 10);

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Should return count")
    void countAll_success() {
        when(purchaseRepositoryPort.countAll()).thenReturn(10L);

        assertEquals(10L, listPurchasesUseCase.countAll());
    }
}
