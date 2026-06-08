package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.DuplicateNitException;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.input.CreateSupplierUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateSupplierUseCaseImpl implements CreateSupplierUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSupplierUseCaseImpl.class);

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    @Transactional
    public SupplierResponse execute(CreateSupplierRequest request) {
        if (supplierRepositoryPort.existsByCodigo(request.getCodigo())) {
            throw new IllegalStateException("Ya existe un proveedor con el código: " + request.getCodigo());
        }
        if (supplierRepositoryPort.existsByNit(request.getNit())) {
            throw new DuplicateNitException(request.getNit());
        }

        Supplier supplier = Supplier.builder()
                .id(UUID.randomUUID())
                .codigo(request.getCodigo())
                .name(request.getName())
                .nit(request.getNit())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(SupplierStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Supplier saved = supplierRepositoryPort.save(supplier);
        log.info("supplier_created codigo={} nit={}", saved.getCodigo(), saved.getNit());
        return toResponse(saved);
    }

    static SupplierResponse toResponse(Supplier s) {
        return SupplierResponse.builder()
                .id(s.getId())
                .codigo(s.getCodigo())
                .name(s.getName())
                .nit(s.getNit())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
