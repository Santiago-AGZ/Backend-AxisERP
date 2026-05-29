package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.domain.exception.DuplicateDocumentException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.input.CreateCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateCustomerUseCaseImpl.class);

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request, UUID createdBy) {
        if (customerRepositoryPort.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new DuplicateDocumentException(request.getDocumentNumber());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepositoryPort.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con el email: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(CustomerStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer saved = customerRepositoryPort.save(customer);
        log.info("customer_created id={} documentNumber={}", saved.getId(), saved.getDocumentNumber());

        return toResponse(saved);
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
