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

import com.axiserp.sales.application.dto.request.UpdateCustomerRequest;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;
import com.axiserp.sales.domain.exception.SaleAccessDeniedException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSaleUseCaseImpl")
class GetSaleUseCaseImplTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private GetSaleUseCaseImpl getSaleUseCase;
    private UUID saleId;
    private Sale sale;

    @BeforeEach
    void setUp() {
        getSaleUseCase = new GetSaleUseCaseImpl(saleRepositoryPort);
        saleId = UUID.randomUUID();
        sale = Sale.builder().id(saleId).status(SaleStatus.BORRADOR).items(List.of()).build();
    }

    @Test
    @DisplayName("Should get sale by id")
    void getById_success() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.of(sale));
        var response = getSaleUseCase.getById(saleId);
        assertEquals(saleId, response.getId());
    }

    @Test
    @DisplayName("Should throw SaleNotFoundException when not found")
    void getById_notFound_throws() {
        when(saleRepositoryPort.findById(saleId)).thenReturn(Optional.empty());
        assertThrows(SaleNotFoundException.class, () -> getSaleUseCase.getById(saleId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ListSalesUseCaseImpl")
class ListSalesUseCaseImplTest {

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private ListSalesUseCaseImpl listSalesUseCase;

    @BeforeEach
    void setUp() {
        listSalesUseCase = new ListSalesUseCaseImpl(saleRepositoryPort);
    }

    @Test
    @DisplayName("Should list sales with filters")
    void list_withFilters_success() {
        when(saleRepositoryPort.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(
                        Sale.builder().status(SaleStatus.BORRADOR).items(List.of()).build(),
                        Sale.builder().status(SaleStatus.CONFIRMADA).items(List.of()).build()));
        when(saleRepositoryPort.countByFilters(any(), any(), any(), any()))
                .thenReturn(2L);

        var response = listSalesUseCase.list(null, null, null, 0, 10);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalRecords());
    }

    @Test
    @DisplayName("Should return empty list when no sales")
    void list_noResults() {
        when(saleRepositoryPort.findByFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(saleRepositoryPort.countByFilters(any(), any(), any(), any()))
                .thenReturn(0L);

        var response = listSalesUseCase.list(null, null, null, 0, 10);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalRecords());
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerUseCaseImpl")
class GetCustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private GetCustomerUseCaseImpl getCustomerUseCase;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        getCustomerUseCase = new GetCustomerUseCaseImpl(customerRepositoryPort);
        customerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get customer by id")
    void getById_success() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(
                Optional.of(Customer.builder().id(customerId).codigo("CLI-000001").status(CustomerStatus.ACTIVO).build()));
        var response = getCustomerUseCase.getById(customerId);
        assertEquals("CLI-000001", response.getCodigo());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found by id")
    void getById_notFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> getCustomerUseCase.getById(customerId));
    }

    @Test
    @DisplayName("Should get customer by codigo")
    void getByCodigo_success() {
        when(customerRepositoryPort.findByCodigo("CLI-000001")).thenReturn(
                Optional.of(Customer.builder().id(customerId).codigo("CLI-000001").status(CustomerStatus.ACTIVO).build()));
        var response = getCustomerUseCase.getByCodigo("CLI-000001");
        assertEquals(customerId, response.getId());
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCustomerUseCaseImpl")
class UpdateCustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private UpdateCustomerUseCaseImpl updateCustomerUseCase;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        updateCustomerUseCase = new UpdateCustomerUseCaseImpl(customerRepositoryPort);
        customerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should update customer")
    void update_success() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(
                Optional.of(Customer.builder().id(customerId).codigo("CLI-000001").status(CustomerStatus.ACTIVO).build()));
        when(customerRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = updateCustomerUseCase.execute(customerId, new UpdateCustomerRequest("New Name", "new@test.com", null, null));
        assertEquals("New Name", response.getName());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void update_notFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class,
                () -> updateCustomerUseCase.execute(customerId, new UpdateCustomerRequest()));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("DeactivateCustomerUseCaseImpl")
class DeactivateCustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private DeactivateCustomerUseCaseImpl deactivateCustomerUseCase;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        deactivateCustomerUseCase = new DeactivateCustomerUseCaseImpl(customerRepositoryPort, saleRepositoryPort);
        customerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should deactivate customer")
    void deactivate_success() {
        Customer customer = Customer.builder().id(customerId).status(CustomerStatus.ACTIVO).build();
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(saleRepositoryPort.existsPendingByCustomerId(customerId)).thenReturn(false);
        when(customerRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = deactivateCustomerUseCase.deactivate(customerId);
        assertEquals(CustomerStatus.INACTIVO.name(), response.getStatus());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void deactivate_notFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> deactivateCustomerUseCase.deactivate(customerId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactivateCustomerUseCaseImpl")
class ReactivateCustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private ReactivateCustomerUseCaseImpl reactivateCustomerUseCase;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        reactivateCustomerUseCase = new ReactivateCustomerUseCaseImpl(customerRepositoryPort);
        customerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should reactivate customer")
    void reactivate_success() {
        Customer customer = Customer.builder().id(customerId).status(CustomerStatus.INACTIVO).build();
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = reactivateCustomerUseCase.reactivate(customerId);
        assertEquals(CustomerStatus.ACTIVO.name(), response.getStatus());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when not found")
    void reactivate_notFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> reactivateCustomerUseCase.reactivate(customerId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("ListCustomersUseCaseImpl")
class ListCustomersUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private ListCustomersUseCaseImpl listCustomersUseCase;

    @BeforeEach
    void setUp() {
        listCustomersUseCase = new ListCustomersUseCaseImpl(customerRepositoryPort);
    }

    @Test
    @DisplayName("Should list customers")
    void list_success() {
        when(customerRepositoryPort.findByFilters(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(
                        Customer.builder().codigo("CLI-000001").status(CustomerStatus.ACTIVO).build(),
                        Customer.builder().codigo("CLI-000002").status(CustomerStatus.ACTIVO).build()));
        when(customerRepositoryPort.countByFilters(any(), anyBoolean()))
                .thenReturn(2L);

        var response = listCustomersUseCase.list(null, false, 0, 20);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalRecords());
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerHistoryUseCaseImpl")
class GetCustomerHistoryUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private GetCustomerHistoryUseCaseImpl getCustomerHistoryUseCase;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        getCustomerHistoryUseCase = new GetCustomerHistoryUseCaseImpl(customerRepositoryPort, saleRepositoryPort);
        customerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return customer history")
    void execute_success() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(
                Optional.of(Customer.builder().id(customerId).codigo("CLI-000001").status(CustomerStatus.ACTIVO).build()));
        when(saleRepositoryPort.findByCustomerId(eq(customerId), any())).thenReturn(List.of(
                Sale.builder().status(SaleStatus.CONFIRMADA).items(List.of()).build()));

        var response = getCustomerHistoryUseCase.execute(customerId);
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer not found")
    void execute_customerNotFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> getCustomerHistoryUseCase.execute(customerId));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoiceUseCaseImpl")
class GetInvoiceUseCaseImplTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;
    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    private GetInvoiceUseCaseImpl getInvoiceUseCase;
    private UUID invoiceId;
    private UUID saleId;

    @BeforeEach
    void setUp() {
        getInvoiceUseCase = new GetInvoiceUseCaseImpl(invoiceRepositoryPort, saleRepositoryPort);
        invoiceId = UUID.randomUUID();
        saleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return invoice by id")
    void getById_success() {
        when(invoiceRepositoryPort.findById(invoiceId)).thenReturn(
                Optional.of(com.axiserp.sales.domain.model.Invoice.builder()
                        .id(invoiceId).saleId(saleId).invoiceNumber(1L).build()));
        when(saleRepositoryPort.findById(saleId)).thenReturn(
                Optional.of(Sale.builder().id(saleId).status(SaleStatus.CONFIRMADA).build()));
        var response = getInvoiceUseCase.getById(invoiceId);
        assertEquals(1L, response.getInvoiceNumber());
    }
}
