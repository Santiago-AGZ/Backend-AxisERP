package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public SupplierResponse execute(CreateSupplierRequest request) {
        if (supplierRepositoryPort.existsByNit(request.getNit())) {
            throw new DuplicateNitException(request.getNit());
        }

        Supplier supplier = Supplier.builder()
                .id(UUID.randomUUID())
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
        log.info("supplier_created id={} nit={}", saved.getId(), saved.getNit());
        return toResponse(saved);
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .nit(supplier.getNit())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
