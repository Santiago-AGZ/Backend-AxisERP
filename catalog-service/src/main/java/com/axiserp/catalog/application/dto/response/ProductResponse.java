package com.axiserp.catalog.application.dto.response;

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
public class ProductResponse {

    private UUID id;
    private String name;
    private String codigo;
    private String description;
    private CategorySummary category;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal margin;
    private BigDecimal marginPercentage;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private UUID id;
        private String name;
    }
}
