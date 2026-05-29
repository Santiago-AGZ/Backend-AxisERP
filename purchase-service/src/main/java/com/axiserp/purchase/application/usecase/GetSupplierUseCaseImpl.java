package com.axiserp.purchase.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.ports.input.GetSupplierUseCase;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSupplierUseCaseImpl implements GetSupplierUseCase {

    private final SupplierRepositoryPort supplierRepositoryPort;

    @Override
    public SupplierResponse execute(UUID id) {
        return supplierRepositoryPort.findById(id)
                .map(CreateSupplierUseCaseImpl::toResponse)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    @Override
    public SupplierResponse executeByCodigo(String codigo) {
        return supplierRepositoryPort.findByCodigo(codigo)
                .map(CreateSupplierUseCaseImpl::toResponse)
                .orElseThrow(() -> new SupplierNotFoundException(codigo));
    }
}
