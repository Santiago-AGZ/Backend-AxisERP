package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.request.UpdateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;

public interface UpdateCustomerUseCase {
    CustomerResponse execute(UUID id, UpdateCustomerRequest request);
}
