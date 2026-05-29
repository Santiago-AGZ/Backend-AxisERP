package com.axiserp.purchase.ports.input;

import java.util.List;

import com.axiserp.purchase.application.dto.response.SupplierResponse;

public interface ListSuppliersUseCase {
    List<SupplierResponse> execute();
}
