package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.sales.domain.model.SaleStatus;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.SaleItemEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaSaleRepository;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepositoryPort {

    private Sale toDomain(SaleEntity entity) {
        Sale sale = Sale.builder().createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        sale.setVersion(entity.getVersion());
        return sale;
    }

    private SaleItem itemToDomain(SaleItemEntity entity) {
        return SaleItem.builder()
                .id(entity.getId())
                .saleId(entity.getSale() != null ? entity.getSale().getId() : null)
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .discount(entity.getDiscount())
                .subtotal(entity.getSubtotal())
                .build();
    }

    private SaleEntity toEntity(Sale domain) {
        SaleEntity entity = SaleEntity.builder()
                .id(domain.getId())
                .customerId(domain.getCustomerId())
                .saleNumber(domain.getSaleNumber())
                .status(SaleEntity.SaleStatus.valueOf(domain.getStatus().name()))
                .subtotal(domain.getSubtotal())
                .discount(domain.getDiscount())
                .tax(domain.getTax())
                .total(domain.getTotal())
                .notes(domain.getNotes())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        entity.setVersion(domain.getVersion());

        if (domain.getItems() != null) {
            List<SaleItemEntity> itemEntities = new ArrayList<>();
            for (SaleItem item : domain.getItems()) {
                SaleItemEntity itemEntity = SaleItemEntity.builder()
                        .id(item.getId())
                        .sale(entity)
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .subtotal(item.getSubtotal())
                        .build();
                itemEntities.add(itemEntity);
            }
            entity.setItems(itemEntities);
        }

        return entity;
    }
}
