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
import com.axiserp.sales.ports.input.ReactivateCustomerUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReactivateCustomerUseCaseImpl implements ReactivateCustomerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReactivateCustomerUseCaseImpl.class);

    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    @Transactional
    public CustomerResponse reactivate(UUID id) {
        Customer customer = customerRepositoryPort.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (customer.getStatus() == CustomerStatus.ACTIVO) {
            throw new IllegalStateException("El cliente ya esta activo");
        }

        customer.setStatus(CustomerStatus.ACTIVO);
        customer.setUpdatedAt(LocalDateTime.now());

        Customer saved = customerRepositoryPort.save(customer);
        log.info("customer_reactivated codigo={}", saved.getCodigo());
        return CreateCustomerUseCaseImpl.toResponse(saved);
    }
}
