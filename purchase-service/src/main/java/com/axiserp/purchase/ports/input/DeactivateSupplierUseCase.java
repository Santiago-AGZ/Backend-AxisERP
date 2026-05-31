package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.response.SupplierResponse;

public interface DeactivateSupplierUseCase {
    SupplierResponse execute(UUID id);
}
