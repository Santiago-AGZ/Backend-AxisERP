package com.axiserp.purchase.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.request.PurchaseItemRequest;
import com.axiserp.purchase.application.dto.response.PurchaseItemResponse;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.exception.DuplicateProductInPurchaseException;
import com.axiserp.purchase.domain.exception.SupplierInactiveException;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.ports.input.CreatePurchaseUseCase;
import com.axiserp.purchase.application.service.AuditService;
import com.axiserp.purchase.ports.output.CatalogServicePort;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePurchaseUseCaseImpl implements CreatePurchaseUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePurchaseUseCaseImpl.class);

    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");

    private final PurchaseRepositoryPort purchaseRepositoryPort;
    private final SupplierRepositoryPort supplierRepositoryPort;
    private final CatalogServicePort catalogServicePort;
    private final AuditService auditService;

    @Override
    @Transactional
    public PurchaseResponse execute(CreatePurchaseRequest request, UUID createdBy) {
        // 1. Verify supplier exists and is active
        Supplier supplier = supplierRepositoryPort.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(request.getSupplierId()));

        if (!supplier.isActive()) {
            throw new SupplierInactiveException(request.getSupplierId());
        }

        // 2 & 3. Verify no duplicate productIds and products exist (R8)
        Set<UUID> seen = new HashSet<>();
        for (PurchaseItemRequest item : request.getItems()) {
            if (!seen.add(item.getProductId())) {
                throw new DuplicateProductInPurchaseException(item.getProductId());
            }
            if (!catalogServicePort.productExists(item.getProductId())) {
                throw new IllegalArgumentException("Producto no encontrado: " + item.getProductId());
            }
        }

        // 4 & 5. Build items with per-item subtotal (validation annotations handle qty>0 and price>0)
        UUID purchaseId = UUID.randomUUID();
        List<PurchaseItem> items = new ArrayList<>();
        for (PurchaseItemRequest itemReq : request.getItems()) {
            BigDecimal itemSubtotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            items.add(PurchaseItem.builder()
                    .id(UUID.randomUUID())
                    .purchaseId(purchaseId)
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .receivedQuantity(0)
                    .unitPrice(itemReq.getUnitPrice())
                    .subtotal(itemSubtotal)
                    .build());
        }

        // 6 & 7 & 8. Calculate totals
        BigDecimal subtotal = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(IVA_RATE);
        BigDecimal total = subtotal.add(tax);

        // 9 & 10. Generate purchase number and build Purchase
        String purchaseNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Purchase purchase = Purchase.builder()
                .id(purchaseId)
                .supplierId(request.getSupplierId())
                .purchaseNumber(purchaseNumber)
                .status(PurchaseStatus.BORRADOR)
                .items(items)
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Purchase saved = purchaseRepositoryPort.save(purchase);
        auditService.logCreate("PURCHASE", saved.getId(), createdBy, "purchase_number=" + saved.getPurchaseNumber());
        log.info("purchase_created id={} purchaseNumber={} supplierId={} total={}",
                saved.getId(), saved.getPurchaseNumber(), saved.getSupplierId(), saved.getTotal());
        return toResponse(saved);
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        List<PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                .map(item -> PurchaseItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .receivedQuantity(item.getReceivedQuantity())
                        .pendingQuantity(item.pendingQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .supplierId(purchase.getSupplierId())
                .purchaseNumber(purchase.getPurchaseNumber())
                .status(purchase.getStatus())
                .items(itemResponses)
                .subtotal(purchase.getSubtotal())
                .tax(purchase.getTax())
                .total(purchase.getTotal())
                .notes(purchase.getNotes())
                .createdBy(purchase.getCreatedBy())
                .updatedBy(purchase.getUpdatedBy())
                .createdAt(purchase.getCreatedAt())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }
}
