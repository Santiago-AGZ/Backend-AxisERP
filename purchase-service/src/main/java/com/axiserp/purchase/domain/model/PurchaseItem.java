package com.axiserp.purchase.domain.model;

import java.math.BigDecimal;
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
public class PurchaseItem {

    private UUID id;
    private UUID purchaseId;
    private UUID productId;
    private String productName;
    private int quantity;
    private int receivedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public int pendingQuantity() {
        return quantity - receivedQuantity;
    }

    public boolean isFullyReceived() {
        return receivedQuantity >= quantity;
    }
}
