package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.input.ReactivateSupplierUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReactivateSupplierUseCaseImpl implements ReactivateSupplierUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReactivateSupplierUseCaseImpl.class);

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    @Transactional
    public SupplierResponse execute(UUID id) {
        Supplier supplier = supplierRepositoryPort.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (supplier.getStatus() == SupplierStatus.ACTIVO) {
            throw new IllegalStateException("El proveedor ya esta activo");
        }

        supplier.setStatus(SupplierStatus.ACTIVO);
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier saved = supplierRepositoryPort.save(supplier);
        log.info("supplier_reactivated codigo={}", saved.getCodigo());
        return CreateSupplierUseCaseImpl.toResponse(saved);
    }
}
