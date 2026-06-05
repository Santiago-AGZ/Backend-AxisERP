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

    private final JpaSaleRepository jpaSaleRepository;

    @Override
    public Optional<Sale> findById(UUID id) {
        return jpaSaleRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Sale save(Sale sale) {
        SaleEntity entity = toEntity(sale);
        SaleEntity saved = jpaSaleRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsPendingByCustomerId(UUID customerId) {
        return jpaSaleRepository.existsByCustomerIdAndStatusIn(
                customerId,
                List.of(SaleEntity.SaleStatus.PENDIENTE, SaleEntity.SaleStatus.CONFIRMADA));
    }

    @Override
    public List<Sale> findByCustomerId(UUID customerId, UUID createdBy) {
        return jpaSaleRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, createdBy)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Sale> findByFilters(UUID customerId, String status, UUID productId, UUID createdBy, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        SaleEntity.SaleStatus statusEnum = (status != null && !status.isBlank()) ? SaleEntity.SaleStatus.valueOf(status) : null;
        return jpaSaleRepository.findByFilters(customerId, statusEnum, productId, createdBy, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Sale toDomain(SaleEntity entity) {
        List<SaleItem> items = entity.getItems() != null
                ? entity.getItems().stream().map(this::itemToDomain).toList()
                : List.of();

        return Sale.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .saleNumber(entity.getSaleNumber())
                .status(SaleStatus.valueOf(entity.getStatus().name()))
                .items(items)
                .subtotal(entity.getSubtotal())
                .discount(entity.getDiscount())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
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
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

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
