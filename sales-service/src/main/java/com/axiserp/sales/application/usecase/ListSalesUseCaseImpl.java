package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.ports.input.ListSalesUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListSalesUseCaseImpl implements ListSalesUseCase {

    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> list(UUID customerId, String status, int page, int size) {
        return saleRepositoryPort.findByFilters(customerId, status, page, size)
                .stream()
                .map(GetSaleUseCaseImpl::toResponse)
                .toList();
    }
}
