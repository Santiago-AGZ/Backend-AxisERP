package com.axiserp.purchase.infrastructure.adapters.out.persistence.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.purchase.domain.model.Purchase;
import com.axiserp.purchase.domain.model.PurchaseItem;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.PurchaseItemEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaPurchaseRepository;
import com.axiserp.purchase.ports.output.PurchaseRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PurchaseRepositoryAdapter implements PurchaseRepositoryPort {

    private final JpaPurchaseRepository jpaPurchaseRepository;

    @Override
    public Optional<Purchase> findById(UUID id) {
        return jpaPurchaseRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = toEntity(purchase);
        PurchaseEntity saved = jpaPurchaseRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Purchase> findAll() {
        return jpaPurchaseRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Purchase> findBySupplierId(UUID supplierId) {
        return jpaPurchaseRepository.findBySupplierId(supplierId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Purchase toDomain(PurchaseEntity entity) {
        List<PurchaseItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .toList();

        return Purchase.builder()
                .id(entity.getId())
                .supplierId(entity.getSupplierId())
                .purchaseNumber(entity.getPurchaseNumber())
                .status(PurchaseStatus.valueOf(entity.getStatus().name()))
                .items(items)
                .subtotal(entity.getSubtotal())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private PurchaseItem toItemDomain(PurchaseItemEntity entity) {
        return PurchaseItem.builder()
                .id(entity.getId())
                .purchaseId(entity.getPurchaseId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .receivedQuantity(entity.getReceivedQuantity())
                .unitPrice(entity.getUnitPrice())
                .subtotal(entity.getSubtotal())
                .build();
    }

    private PurchaseEntity toEntity(Purchase domain) {
        List<PurchaseItemEntity> itemEntities = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemEntity).toList()
                : new ArrayList<>();

        return PurchaseEntity.builder()
                .id(domain.getId())
                .supplierId(domain.getSupplierId())
                .purchaseNumber(domain.getPurchaseNumber())
                .status(PurchaseEntity.PurchaseStatus.valueOf(domain.getStatus().name()))
                .items(itemEntities)
                .subtotal(domain.getSubtotal())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .notes(domain.getNotes())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private PurchaseItemEntity toItemEntity(PurchaseItem item) {
        return PurchaseItemEntity.builder()
                .id(item.getId())
                .purchaseId(item.getPurchaseId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
