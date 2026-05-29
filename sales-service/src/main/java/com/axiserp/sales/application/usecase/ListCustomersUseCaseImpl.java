package com.axiserp.sales.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.ports.input.ListCustomersUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCustomersUseCaseImpl implements ListCustomersUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    public List<CustomerResponse> list(String search, boolean includeInactive, int page, int size) {
        return customerRepositoryPort.findByFilters(search, includeInactive, page, size)
                .stream()
                .map(CreateCustomerUseCaseImpl::toResponse)
                .toList();
    }
}
