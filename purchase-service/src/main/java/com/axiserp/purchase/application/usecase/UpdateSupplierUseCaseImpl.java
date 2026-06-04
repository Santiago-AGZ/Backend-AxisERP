package com.axiserp.purchase.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.request.UpdateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.ports.input.UpdateSupplierUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateSupplierUseCaseImpl implements UpdateSupplierUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateSupplierUseCaseImpl.class);

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    public SupplierResponse execute(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepositoryPort.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier saved = supplierRepositoryPort.save(supplier);
        log.info("supplier_updated id={}", saved.getId());
        return CreateSupplierUseCaseImpl.toResponse(saved);
    }
}
