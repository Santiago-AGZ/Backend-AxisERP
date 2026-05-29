package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.input.DeactivateCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeactivateCustomerUseCaseImpl implements DeactivateCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateCustomerUseCaseImpl.class);

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional
    public CustomerResponse deactivate(UUID id) {
        Customer customer = customerRepositoryPort.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setStatus(CustomerStatus.INACTIVO);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepositoryPort.save(customer);
        log.info("customer_deactivated id={}", saved.getId());

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
