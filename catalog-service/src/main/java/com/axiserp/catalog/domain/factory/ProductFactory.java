package com.axiserp.catalog.domain.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.axiserp.catalog.domain.model.Product;
import com.axiserp.catalog.domain.model.Product.ProductStatus;

public final class ProductFactory {

    private ProductFactory() {}

    public static Product createNew(String name, String codigo, UUID categoryId,
            BigDecimal purchasePrice, BigDecimal salePrice, UUID createdBy) {
        return Product.builder()
                .name(name)
                .codigo(codigo)
                .categoryId(categoryId)
                .purchasePrice(purchasePrice)
                .salePrice(salePrice)
                .status(ProductStatus.ACTIVO)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Product update(Product existing, String name, UUID categoryId,
            BigDecimal purchasePrice, BigDecimal salePrice) {
        return Product.builder()
                .id(existing.getId())
                .name(name != null ? name : existing.getName())
                .codigo(existing.getCodigo())
                .categoryId(categoryId != null ? categoryId : existing.getCategoryId())
                .purchasePrice(purchasePrice != null ? purchasePrice : existing.getPurchasePrice())
                .salePrice(salePrice != null ? salePrice : existing.getSalePrice())
                .status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Product deactivate(Product existing) {
        return Product.builder()
                .id(existing.getId())
                .name(existing.getName())
                .codigo(existing.getCodigo())
                .categoryId(existing.getCategoryId())
                .purchasePrice(existing.getPurchasePrice())
                .salePrice(existing.getSalePrice())
                .status(ProductStatus.INACTIVO)
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Product softDelete(Product existing) {
        return Product.builder()
                .id(existing.getId())
                .name(existing.getName())
                .codigo(existing.getCodigo())
                .categoryId(existing.getCategoryId())
                .purchasePrice(existing.getPurchasePrice())
                .salePrice(existing.getSalePrice())
                .status(ProductStatus.ELIMINADO)
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
