package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleItemResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.ports.input.GetSaleUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSaleUseCaseImpl implements GetSaleUseCase {

    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(UUID id) {
        Sale sale = saleRepositoryPort.findById(id)
                .orElseThrow(() -> new SaleNotFoundException(id));
        return toResponse(sale);
    }

    static SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems() != null
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
                .items(itemResponses)
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
