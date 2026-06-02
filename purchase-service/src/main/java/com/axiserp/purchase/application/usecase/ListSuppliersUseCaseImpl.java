package com.axiserp.purchase.application.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.ports.input.ListSuppliersUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListSuppliersUseCaseImpl implements ListSuppliersUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListSuppliersUseCaseImpl.class);

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    public List<SupplierResponse> execute() {
        List<Supplier> suppliers = supplierRepositoryPort.findAllActive();
        log.info("suppliers_list count={}", suppliers.size());
        return suppliers.stream().map(this::toResponse).toList();
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .codigo(supplier.getCodigo())
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
