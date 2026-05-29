package com.axiserp.purchase.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.ports.input.GetSupplierUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSupplierUseCaseImpl implements GetSupplierUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSupplierUseCaseImpl.class);

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    public SupplierResponse execute(UUID id) {
        Supplier supplier = supplierRepositoryPort.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        log.info("supplier_get id={}", id);
        return toResponse(supplier);
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
