package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.CustomerResponse;

public interface DeactivateCustomerUseCase {

    CustomerResponse deactivate(UUID id);
}
