package com.axiserp.purchase.ports.input;

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;

public interface CreateSupplierUseCase {
    SupplierResponse execute(CreateSupplierRequest request);
}
