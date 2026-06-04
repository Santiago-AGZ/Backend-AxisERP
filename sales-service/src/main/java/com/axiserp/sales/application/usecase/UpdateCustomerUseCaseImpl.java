package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.request.UpdateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.ports.input.UpdateCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCaseImpl implements UpdateCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateCustomerUseCaseImpl.class);

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional
    public CustomerResponse execute(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepositoryPort.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(customer.getEmail())
                && customerRepositoryPort.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalStateException("Ya existe un cliente con el email: " + request.getEmail());
        }

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        customer.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepositoryPort.save(customer);
        log.info("customer_updated id={} codigo={}", saved.getId(), saved.getCodigo());

        return CreateCustomerUseCaseImpl.toResponse(saved);
    }
}
