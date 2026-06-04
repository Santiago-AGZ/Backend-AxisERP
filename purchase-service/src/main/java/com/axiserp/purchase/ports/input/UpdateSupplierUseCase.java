package com.axiserp.purchase.ports.input;

import java.util.UUID;

import com.axiserp.purchase.application.dto.request.UpdateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;

public interface UpdateSupplierUseCase {
    SupplierResponse execute(UUID id, UpdateSupplierRequest request);
}
