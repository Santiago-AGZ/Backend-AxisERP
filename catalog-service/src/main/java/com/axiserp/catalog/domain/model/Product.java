package com.axiserp.catalog.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID id;
    private String name;
    private String codigo;
    private String description;
    private UUID categoryId;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal profitMargin;
    private ProductStatus status;
    private Long version;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        ACTIVO, INACTIVO, ELIMINADO
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVO;
    }

    public boolean isDeleted() {
        return this.status == ProductStatus.ELIMINADO;
    }

    public boolean hasValidMargin() {
        return salePrice.compareTo(purchasePrice) >= 0;
    }

    public BigDecimal getMargin() {
        return salePrice.subtract(purchasePrice);
    }

    public BigDecimal getMarginPercentage() {
        return purchasePrice.compareTo(BigDecimal.ZERO) > 0
                ? getMargin().divide(purchasePrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
    }
}
