package com.axiserp.purchase.domain.model;

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
public class Purchase {

    private UUID id;
    private UUID supplierId;
    private String purchaseNumber;
    private PurchaseStatus status;
    private List<PurchaseItem> items;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private String notes;
    private Long version;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isModifiable() {
        return status != null && status != PurchaseStatus.CANCELADA && status != PurchaseStatus.PAGADA;
    }
}
