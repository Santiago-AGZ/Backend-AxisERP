package com.axiserp.purchase.infrastructure.adapters.out.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.entity.SupplierEntity;
import com.axiserp.purchase.infrastructure.adapters.out.persistence.repository.JpaSupplierRepository;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements SupplierRepositoryPort {

    private final JpaSupplierRepository jpaSupplierRepository;

    @Override
    public Optional<Supplier> findById(UUID id) {
        return jpaSupplierRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Supplier> findByNit(String nit) {
        return jpaSupplierRepository.findByNit(nit).map(this::toDomain);
    }

    @Override
    public boolean existsByNit(String nit) {
        return jpaSupplierRepository.existsByNit(nit);
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierEntity entity = toEntity(supplier);
        SupplierEntity saved = jpaSupplierRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Supplier> findAllActive() {
        return jpaSupplierRepository
                .findByStatusOrderByNameAsc(SupplierEntity.SupplierStatus.ACTIVO)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findAll() {
        return jpaSupplierRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Supplier toDomain(SupplierEntity entity) {
        return Supplier.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nit(entity.getNit())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .status(SupplierStatus.valueOf(entity.getStatus().name()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SupplierEntity toEntity(Supplier domain) {
        return SupplierEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .nit(domain.getNit())
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .address(domain.getAddress())
                .status(SupplierEntity.SupplierStatus.valueOf(domain.getStatus().name()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
