package com.axiserp.sales.domain.model;

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
public class SaleItem {

    private UUID id;
    private UUID saleId;
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subtotal;
}
