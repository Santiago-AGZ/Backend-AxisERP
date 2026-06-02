package com.axiserp.sales.application.dto.response;

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
public class SaleResponse {

    private UUID id;
    private UUID customerId;
    private String saleNumber;
    private String status;
    private List<SaleItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private String notes;
    private UUID createdBy;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
