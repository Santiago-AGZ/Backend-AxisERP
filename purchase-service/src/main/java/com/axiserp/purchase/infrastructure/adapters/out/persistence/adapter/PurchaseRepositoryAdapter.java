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

    private PurchaseEntity toEntity(Purchase domain) {
        PurchaseEntity entity = PurchaseEntity.builder().createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        entity.setVersion(domain.getVersion());
        return entity;
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
