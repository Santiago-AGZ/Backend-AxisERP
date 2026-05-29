package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.ports.input.GetCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCustomerUseCaseImpl implements GetCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepositoryPort.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .documentType(customer.getDocumentType())
                .documentNumber(customer.getDocumentNumber())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .status(customer.getStatus().name())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
