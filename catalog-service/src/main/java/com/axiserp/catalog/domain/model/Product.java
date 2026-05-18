package com.axiserp.catalog.domain.model;

import java.math.BigDecimal;
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
    private UUID categoryId;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private ProductStatus status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        ACTIVO, INACTIVO
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVO;
    }

    public boolean hasValidMargin() {
        return salePrice.compareTo(purchasePrice) >= 0;
    }
}
