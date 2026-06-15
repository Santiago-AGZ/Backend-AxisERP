package com.axiserp.sales.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.infrastructure.adapters.out.persistence.entity.CustomerEntity;
import com.axiserp.sales.infrastructure.adapters.out.persistence.repository.JpaCustomerRepository;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final JpaCustomerRepository jpaCustomerRepository;

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaCustomerRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByCodigo(String codigo) {
        return jpaCustomerRepository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaCustomerRepository.existsByCodigo(codigo);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return jpaCustomerRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaCustomerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return jpaCustomerRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public Customer save(Customer customer) {
        return toDomain(jpaCustomerRepository.save(toEntity(customer)));
    }

    @Override
    public List<Customer> findByFilters(String search, boolean includeInactive, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        boolean hasSearch = search != null && !search.isBlank();
        String pattern = hasSearch ? "%" + search.toLowerCase() + "%" : "%";
        return jpaCustomerRepository.findByFilters(
                        hasSearch,
                        pattern,
                        includeInactive,
                        CustomerEntity.CustomerStatus.ACTIVO,
                        pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(String search, boolean includeInactive) {
        boolean hasSearch = search != null && !search.isBlank();
        String pattern = hasSearch ? "%" + search.toLowerCase() + "%" : "%";
        return jpaCustomerRepository.countByFilters(hasSearch, pattern, includeInactive, CustomerEntity.CustomerStatus.ACTIVO);
    }

    private Customer toDomain(CustomerEntity e) {
        return Customer.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .name(e.getName())
                .documentType(e.getDocumentType() != null ? e.getDocumentType().name() : null)
                .documentNumber(e.getDocumentNumber())
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(e.getAddress())
                .status(CustomerStatus.valueOf(e.getStatus().name()))
                .version(e.getVersion())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private CustomerEntity toEntity(Customer c) {
        return CustomerEntity.builder()
                .id(c.getId())
                .codigo(c.getCodigo())
                .name(c.getName())
                .documentType(c.getDocumentType() != null
                        ? CustomerEntity.DocumentType.valueOf(c.getDocumentType())
                        : null)
                .documentNumber(c.getDocumentNumber())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .status(CustomerEntity.CustomerStatus.valueOf(c.getStatus().name()))
                .version(c.getVersion())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
