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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.request.SaleItemRequest;
import com.axiserp.sales.application.dto.response.ProductSummary;
import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.CustomerInactiveException;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.exception.DuplicateProductInSaleException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.output.CatalogServicePort;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSaleUseCaseImpl")
class CreateSaleUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;
    @Mock
    private SaleRepositoryPort saleRepositoryPort;
    @Mock
    private CatalogServicePort catalogServicePort;
    @Mock
    private AuditService auditService;

    private CreateSaleUseCaseImpl createSaleUseCase;
    private UUID customerId;
    private UUID productId;
    private UUID createdBy;
    private Customer activeCustomer;
    private CreateSaleRequest request;

    @BeforeEach
    void setUp() {
        createSaleUseCase = new CreateSaleUseCaseImpl(customerRepositoryPort, saleRepositoryPort, catalogServicePort, auditService);
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        createdBy = UUID.randomUUID();

        activeCustomer = Customer.builder()
                .id(customerId)
                .codigo("CLI-000001")
                .name("Test")
                .status(CustomerStatus.ACTIVO)
                .build();

        request = CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(SaleItemRequest.builder()
                        .productId(productId)
                        .productName("Product Test")
                        .quantity(5)
                        .unitPrice(new BigDecimal("100.00"))
                        .discount(BigDecimal.ZERO)
                        .build()))
                .discount(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Should create sale successfully")
    void create_success() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = createSaleUseCase.create(request, createdBy, false);

        assertNotNull(response);
        assertEquals(SaleStatus.BORRADOR.name(), response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(0, response.getTotal().compareTo(new BigDecimal("595.00")));
        assertTrue(response.getSaleNumber().startsWith("VN-"));
        verify(saleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer not found")
    void create_customerNotFound_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> createSaleUseCase.create(request, createdBy, false));
    }

    @Test
    @DisplayName("Should throw CustomerInactiveException when customer inactive")
    void create_customerInactive_throws() {
        Customer inactive = Customer.builder().id(customerId).status(CustomerStatus.INACTIVO).build();
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(inactive));

        assertThrows(CustomerInactiveException.class,
                () -> createSaleUseCase.create(request, createdBy, false));
    }

    @Test
    @DisplayName("Should throw DuplicateProductInSaleException for duplicate products")
    void create_duplicateProduct_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));

        CreateSaleRequest dupRequest = CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(
                        SaleItemRequest.builder().productId(productId).productName("A").quantity(1).unitPrice(new BigDecimal("10")).discount(BigDecimal.ZERO).build(),
                        SaleItemRequest.builder().productId(productId).productName("A dup").quantity(1).unitPrice(new BigDecimal("10")).discount(BigDecimal.ZERO).build()))
                .discount(BigDecimal.ZERO)
                .build();

        assertThrows(DuplicateProductInSaleException.class,
                () -> createSaleUseCase.create(dupRequest, createdBy, false));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for discount without admin")
    void create_discountWithoutAdmin_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));

        CreateSaleRequest discRequest = CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(SaleItemRequest.builder().productId(productId).productName("A").quantity(1).unitPrice(new BigDecimal("10")).discount(BigDecimal.ZERO).build()))
                .discount(new BigDecimal("5.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> createSaleUseCase.create(discRequest, createdBy, false));
    }

    @Test
    @DisplayName("Should create sale with discount when admin")
    void create_withDiscountAsAdmin_success() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateSaleRequest discRequest = CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(SaleItemRequest.builder()
                        .productId(productId).productName("A").quantity(5)
                        .unitPrice(new BigDecimal("100.00")).discount(BigDecimal.ZERO).build()))
                .discount(new BigDecimal("50.00"))
                .build();

        var response = createSaleUseCase.create(discRequest, createdBy, true);

        assertNotNull(response);
        assertEquals(0, response.getDiscount().compareTo(new BigDecimal("50.00")));
        verify(saleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("Should throw when discount exceeds 30%")
    void create_discountExceeds30percent_throws() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        CreateSaleRequest discRequest = CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(SaleItemRequest.builder()
                        .productId(productId).productName("A").quantity(5)
                        .unitPrice(new BigDecimal("100.00")).discount(BigDecimal.ZERO).build()))
                .discount(new BigDecimal("200.00"))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> createSaleUseCase.create(discRequest, createdBy, true));
    }
}
