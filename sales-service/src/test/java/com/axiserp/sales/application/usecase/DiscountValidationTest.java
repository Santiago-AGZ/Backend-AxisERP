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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.request.SaleItemRequest;
import com.axiserp.sales.application.dto.response.ProductSummary;
import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.SaleAccessDeniedException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.output.CatalogServicePort;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("[R13-R14] Discount Validation Audit")
class DiscountValidationTest {

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
                .name("Test Customer")
                .status(CustomerStatus.ACTIVO)
                .build();
    }

    private CreateSaleRequest saleRequest(int qty, BigDecimal price, BigDecimal discountPercent) {
        return CreateSaleRequest.builder()
                .customerId(customerId)
                .items(List.of(SaleItemRequest.builder()
                        .productId(productId)
                        .productName("Product Test")
                        .quantity(qty)
                        .unitPrice(price)
                        .build()))
                .discount(discountPercent)
                .build();
    }

    private void mockSuccess() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId))
                .thenReturn(new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void mockFind() {
        when(customerRepositoryPort.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(catalogServicePort.findProductSummary(productId))
                .thenReturn(new ProductSummary(productId, "Product Test", "PROD-001", "ACTIVO"));
        lenient().when(saleRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("R14 — Max 30% discount (ADMIN role)")
    class R14_MaxDiscount {

        @Test
        @DisplayName("15% of $1000 — PASS (well below 30%)")
        void discount15_on1000_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(10, new BigDecimal("100.00"), new BigDecimal("15")), userId, true);
            assertNotNull(resp);
        }

        @Test
        @DisplayName("29.99% of $1000 — PASS (just below 30%)")
        void discount2999_on1000_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(10, new BigDecimal("100.00"), new BigDecimal("29.99")), userId, true);
            assertNotNull(resp);
        }

        @Test
        @DisplayName("30.00% of $1000 — PASS (boundary)")
        void discount3000_on1000_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(10, new BigDecimal("100.00"), new BigDecimal("30.00")), userId, true);
            assertNotNull(resp);
        }

        @Test
        @DisplayName("30.01% of $1000 — FAIL (exceeds 30%)")
        void discount3001_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("30.01")), userId, true));
            verify(saleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("35% of $1000 — FAIL (exceeds 30% — exposes percentage-vs-money bug)")
        void discount35_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("35")), userId, true));
            verify(saleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("15% of $50 — PASS (well below 30%)")
        void discount15_on50_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(1, new BigDecimal("50.00"), new BigDecimal("15")), userId, true);
            assertNotNull(resp);
        }

        @Test
        @DisplayName("40% of $50 — FAIL (exceeds 30%)")
        void discount40_on50_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(1, new BigDecimal("50.00"), new BigDecimal("40")), userId, true));
            verify(saleRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("R13 — Any discount requires ADMIN role")
    class R13_AdminAuthorization {

        @Test
        @DisplayName("VENDEDOR with 0% discount — PASS (no discount)")
        void vendedor_zeroDiscount_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(1, new BigDecimal("100.00"), BigDecimal.ZERO), userId, false);
            assertNotNull(resp);
        }

        @Test
        @DisplayName("VENDEDOR with 5% discount on $1000 — FAIL (needs ADMIN)")
        void vendedor_5percent_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("5")), userId, false));
            verify(saleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("VENDEDOR with 1% discount on $1000 — FAIL (needs ADMIN)")
        void vendedor_1percent_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("1")), userId, false));
            verify(saleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("ADMIN with 15% discount on $1000 — PASS (admin authorized)")
        void admin_15percent_on1000_passes() {
            mockSuccess();
            var resp = createSaleUseCase.create(
                    saleRequest(10, new BigDecimal("100.00"), new BigDecimal("15")), userId, true);
            assertNotNull(resp);
        }
    }

    @Nested
    @DisplayName("R14 + R13 — Combined edge cases")
    class CombinedEdgeCases {

        @Test
        @DisplayName("VENDEDOR with 35% on $1000 — FAIL (both R13 and R14)")
        void vendedor_35percent_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("35")), userId, false));
            verify(saleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("ADMIN with 35% on $1000 — FAIL (R14 hard limit)")
        void admin_35percent_on1000_fails() {
            mockFind();
            assertThrows(SaleAccessDeniedException.class,
                    () -> createSaleUseCase.create(
                            saleRequest(10, new BigDecimal("100.00"), new BigDecimal("35")), userId, true));
            verify(saleRepositoryPort, never()).save(any());
        }
    }
}
