package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.ports.input.GetCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCustomerUseCaseImpl implements GetCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id) {
        return customerRepositoryPort.findById(id)
                .map(CreateCustomerUseCaseImpl::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByCodigo(String codigo) {
        return customerRepositoryPort.findByCodigo(codigo)
                .map(CreateCustomerUseCaseImpl::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(codigo));
    }
}
