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
import com.axiserp.sales.domain.exception.SaleAccessDeniedException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.output.CatalogServicePort;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("[R13-R14] Discount Authorization Tests")
class DiscountAuthorizationTest {

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
    private UUID userId;
    private Customer activeCustomer;

    @BeforeEach
    void setUp() {
        createSaleUseCase = new CreateSaleUseCaseImpl(
                customerRepositoryPort, saleRepositoryPort, catalogServicePort, auditService);
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();

        activeCustomer = Customer.builder()
                .id(customerId)
                .codigo("CLI-000001")
                .name("Test")
                .status(CustomerStatus.ACTIVO)
                .build();
    }

    private CreateSaleRequest createRequestWithDiscount(BigDecimal discountPercent) {
        return CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(
                        SaleItemRequest.builder()
                                .productId(productId)
                                .productName("Product Test")
                                .quantity(1)
                                .unitPrice(new BigDecimal("100.00"))
                                .build()))
                .discount(discountPercent)
                .build();
    }

    @Test
    @DisplayName("[R13] Should reject non-admin user with ANY discount > 0%")
    void nonAdmin_anyDiscount_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("20"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        assertThrows(SaleAccessDeniedException.class,
                () -> createSaleUseCase.create(request, userId, false));
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should allow non-admin user with 0% discount (no discount)")
    void nonAdmin_zeroDiscount_success() {
        CreateSaleRequest request = createRequestWithDiscount(BigDecimal.ZERO);

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = createSaleUseCase.create(request, userId, false);

        assertNotNull(response);
        assertEquals("BORRADOR", response.getStatus());
    }

    @Test
    @DisplayName("[R14] Should throw SaleAccessDeniedException for non-admin with discount > 30%")
    void nonAdmin_discountAbove30_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("35"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        assertThrows(SaleAccessDeniedException.class,
                () -> createSaleUseCase.create(request, userId, false));
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R14] Should reject admin with discount > 30% (hard limit)")
    void admin_discountAbove30_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("50"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        assertThrows(SaleAccessDeniedException.class,
                () -> createSaleUseCase.create(request, userId, true));
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13-R14] Should reject admin with 100% discount (exceeds 30%)")
    void admin_discount100_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("100"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        assertThrows(SaleAccessDeniedException.class,
                () -> createSaleUseCase.create(request, userId, true));
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13] Should reject non-admin with exactly 30% discount (needs ADMIN)")
    void nonAdmin_discountExact30_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("30"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));

        assertThrows(SaleAccessDeniedException.class,
                () -> createSaleUseCase.create(request, userId, false));
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("[R13+R14] Should allow admin with 15% discount (valid)")
    void admin_discount15_success() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("15"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId)).thenReturn(
                new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = createSaleUseCase.create(request, userId, true);

        assertNotNull(response);
        assertEquals("BORRADOR", response.getStatus());
    }

    @Test
    @DisplayName("[R14] Should reject negative discount")
    void negativeDiscount_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("-10"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));

        assertThrows(IllegalArgumentException.class,
                () -> createSaleUseCase.create(request, userId, false));
    }

    @Test
    @DisplayName("[R14] Should reject discount over 100%")
    void discountOver100_throws() {
        CreateSaleRequest request = createRequestWithDiscount(new BigDecimal("150"));

        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));

        assertThrows(IllegalArgumentException.class,
                () -> createSaleUseCase.create(request, userId, false));
    }
}
