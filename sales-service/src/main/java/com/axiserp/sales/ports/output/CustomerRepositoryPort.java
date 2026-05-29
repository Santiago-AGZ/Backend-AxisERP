package com.axiserp.sales.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.axiserp.sales.domain.model.Customer;

public interface CustomerRepositoryPort {

    Optional<Customer> findById(UUID id);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);

    Customer save(Customer customer);

    List<Customer> findByFilters(String search, boolean includeInactive, int page, int size);
}
