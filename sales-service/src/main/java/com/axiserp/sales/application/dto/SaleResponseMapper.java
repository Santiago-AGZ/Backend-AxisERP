package com.axiserp.sales.application.dto;

import com.axiserp.sales.application.dto.response.SaleItemResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.model.Sale;
import java.util.List;

public final class SaleResponseMapper {
    private SaleResponseMapper() {}

    public static SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems() != null
                ? sale.getItems().stream().map(item -> SaleItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discount(item.getDiscount())
                        .subtotal(item.getSubtotal())
                        .build()).toList()
                : List.of();

        return SaleResponse.builder()
                .id(sale.getId())
                .customerId(sale.getCustomerId())
                .saleNumber(sale.getSaleNumber())
                .status(sale.getStatus().name())
                .items(items)
                .subtotal(sale.getSubtotal())
                .discount(sale.getDiscount())
                .tax(sale.getTax())
                .total(sale.getTotal())
                .notes(sale.getNotes())
                .createdBy(sale.getCreatedBy())
                .version(sale.getVersion())
                .createdAt(sale.getCreatedAt())
                .updatedAt(sale.getUpdatedAt())
                .build();
    }
}
