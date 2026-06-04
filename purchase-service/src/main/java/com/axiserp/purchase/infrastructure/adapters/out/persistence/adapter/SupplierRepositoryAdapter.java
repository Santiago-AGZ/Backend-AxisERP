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
    public Optional<Supplier> findByCodigo(String codigo) {
        return jpaSupplierRepository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public Optional<Supplier> findByNit(String nit) {
        return jpaSupplierRepository.findByNit(nit).map(this::toDomain);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaSupplierRepository.existsByCodigo(codigo);
    }

    @Override
    public boolean existsByNit(String nit) {
        return jpaSupplierRepository.existsByNit(nit);
    }

    @Override
    public boolean existsByNitAndIdNot(String nit, UUID id) {
        return jpaSupplierRepository.existsByNitAndIdNot(nit, id);
    }

    @Override
    public Supplier save(Supplier supplier) {
        return toDomain(jpaSupplierRepository.save(toEntity(supplier)));
    }

    @Override
    public List<Supplier> findAllActive() {
        return jpaSupplierRepository
                .findByStatusOrderByNameAsc(SupplierEntity.SupplierStatus.ACTIVO)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Supplier> findAllActive(String search, int page, int size) {
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        return jpaSupplierRepository.findBySearch(searchParam, size, page * size)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countAllActive() {
        return jpaSupplierRepository.countByStatus(SupplierEntity.SupplierStatus.ACTIVO);
    }

    @Override
    public long countActiveBySearch(String search) {
        String searchParam = (search != null && !search.isBlank()) ? search : null;
        return jpaSupplierRepository.countBySearch(searchParam);
    }

    @Override
    public List<Supplier> findAll() {
        return jpaSupplierRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Supplier toDomain(SupplierEntity e) {
        return Supplier.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .name(e.getName())
                .nit(e.getNit())
                .phone(e.getPhone())
                .email(e.getEmail())
                .address(e.getAddress())
                .status(SupplierStatus.valueOf(e.getStatus().name()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private SupplierEntity toEntity(Supplier s) {
        return SupplierEntity.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .name(s.getName())
                .nit(s.getNit())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .status(SupplierEntity.SupplierStatus.valueOf(s.getStatus().name()))
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
