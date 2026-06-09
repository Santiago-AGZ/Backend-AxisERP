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
        String codigo = request.getCodigo();
        if (codigo == null || codigo.isBlank()) {
            codigo = "CLI-" + java.time.Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else if (customerRepositoryPort.existsByCodigo(codigo)) {
            throw new IllegalStateException("Ya existe un cliente con el código: " + request.getCodigo());
        }
        if (customerRepositoryPort.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new DuplicateDocumentException(request.getDocumentNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepositoryPort.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con el email: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .codigo(codigo)
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
        log.info("customer_created codigo={} documentNumber={}", saved.getCodigo(), saved.getDocumentNumber());

        return toResponse(saved);
    }

    static CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .name(c.getName())
                .documentType(c.getDocumentType())
                .documentNumber(c.getDocumentNumber())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .status(c.getStatus().name())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
