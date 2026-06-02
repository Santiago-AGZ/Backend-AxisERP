package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.response.SupplierResponse;

public interface GetSupplierUseCase {
    SupplierResponse execute(UUID id);
    SupplierResponse executeByCodigo(String codigo);
}
