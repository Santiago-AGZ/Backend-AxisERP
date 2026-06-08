package com.axiserp.sales.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.ports.input.ListCustomersUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCustomersUseCaseImpl implements ListCustomersUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CustomerResponse> list(String search, boolean includeInactive, int page, int size) {
        List<CustomerResponse> content = customerRepositoryPort.findByFilters(search, includeInactive, page, size)
                .stream()
                .map(CreateCustomerUseCaseImpl::toResponse)
                .toList();
        long total = customerRepositoryPort.countByFilters(search, includeInactive);
        return PaginatedResponse.<CustomerResponse>builder()
                .content(content)
                .totalRecords(total)
                .build();
    }
}
