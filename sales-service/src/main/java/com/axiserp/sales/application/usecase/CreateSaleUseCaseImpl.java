package com.axiserp.sales.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.request.SaleItemRequest;
import com.axiserp.sales.application.dto.response.SaleItemResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.CustomerInactiveException;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.exception.DuplicateProductInSaleException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.input.CreateSaleUseCase;
import com.axiserp.sales.ports.output.CatalogServicePort;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateSaleUseCaseImpl implements CreateSaleUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSaleUseCaseImpl.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");

    private final CustomerRepositoryPort customerRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final CatalogServicePort catalogServicePort;
    private final AuditService auditService;

    @Override
    @Transactional
    public SaleResponse create(CreateSaleRequest request, UUID createdBy, boolean isAdmin) {
        // 1. Verify customer exists and is active
        Customer customer = customerRepositoryPort.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        if (!customer.isActive()) {
            throw new CustomerInactiveException(customer.getId());
        }

        // 2. Verify at least one item
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto");
        }

        // 3. Verify no duplicate productId
        Set<UUID> productIds = new HashSet<>();
        for (SaleItemRequest itemReq : request.getItems()) {
            if (!productIds.add(itemReq.getProductId())) {
                throw new DuplicateProductInSaleException(itemReq.getProductId());
            }
        }

        // 3.2 Verify all products exist and are active in catalog
        for (SaleItemRequest itemReq : request.getItems()) {
            var summary = catalogServicePort.findProductSummary(itemReq.getProductId());
            if (summary == null) {
                throw new IllegalArgumentException("El producto " + itemReq.getProductId() + " no existe en el catalogo");
            }
            if (!"ACTIVO".equals(summary.getStatus())) {
                throw new IllegalArgumentException("El producto " + itemReq.getProductId() + " no esta activo");
            }
        }

        // 4. Calculate item subtotals (backend-computed, no discount from request)
        List<SaleItem> items = request.getItems().stream()
                .map(itemReq -> {
                    if (itemReq.getQuantity() <= 0) {
                        throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
                    }
                    if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("El precio unitario debe ser mayor que cero");
                    }
                    BigDecimal itemSubtotal = itemReq.getUnitPrice()
                            .multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

                    return SaleItem.builder()
                            .productId(itemReq.getProductId())
                            .productName(itemReq.getProductName())
                            .quantity(itemReq.getQuantity())
                            .unitPrice(itemReq.getUnitPrice())
                            .discount(BigDecimal.ZERO)
                            .subtotal(itemSubtotal)
                            .build();
                })
                .toList();

        // 5. Calculate totals (backend-computed)
        BigDecimal subtotal = items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saleDiscount = BigDecimal.ZERO;
        BigDecimal taxBase = subtotal;
        BigDecimal tax = taxBase.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxBase.add(tax).setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total de la venta no puede ser negativo");
        }

        // 10. Generate sale number
        String saleNumber = "VN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 11. Create sale with BORRADOR status
        Sale sale = Sale.builder()
                .customerId(customer.getId())
                .saleNumber(saleNumber)
                .status(SaleStatus.BORRADOR)
                .items(items)
                .subtotal(subtotal)
                .discount(saleDiscount)
                .tax(tax)
                .total(total)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Sale saved = saleRepositoryPort.save(sale);
        auditService.logSaleCreated(saved.getId(), createdBy, null,
                String.format("saleNumber=%s customerId=%s total=%s", saved.getSaleNumber(), saved.getCustomerId(), saved.getTotal()));
        log.info("sale_created id={} saleNumber={} customerId={} total={}", saved.getId(), saved.getSaleNumber(), saved.getCustomerId(), saved.getTotal());

        return toResponse(saved);
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems() != null
                ? sale.getItems().stream().map(item -> SaleItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .subtotal(item.getSubtotal())
                        .build()).toList()
                : List.of();

        return SaleResponse.builder()
                .id(sale.getId())
                .customerId(sale.getCustomerId())
                .saleNumber(sale.getSaleNumber())
                .status(sale.getStatus().name())
                .items(itemResponses)
                .subtotal(sale.getSubtotal())
                .discount(sale.getDiscount())
                .tax(sale.getTax())
                .total(sale.getTotal())
                .notes(sale.getNotes())
                .createdBy(sale.getCreatedBy())
                .version(sale.getVersion())
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .build();
    }
}
