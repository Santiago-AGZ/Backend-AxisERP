package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.ports.input.GetCustomerHistoryUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCustomerHistoryUseCaseImpl implements GetCustomerHistoryUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> execute(UUID customerId) {
        customerRepositoryPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return saleRepositoryPort.findByCustomerId(customerId)
                .stream()
                .map(GetSaleUseCaseImpl::toResponse)
                .toList();
    }
}
