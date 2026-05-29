package com.axiserp.sales.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class Sale {

    private UUID id;
    private UUID customerId;
    private String saleNumber;
    private SaleStatus status;
    private List<SaleItem> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private String notes;
    private UUID createdBy;
    private UUID updatedBy;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isModifiable() {
        return status != SaleStatus.ANULADA && status != SaleStatus.PAGADA;
    }
}
