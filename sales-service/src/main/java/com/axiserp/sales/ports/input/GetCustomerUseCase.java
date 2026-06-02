package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.response.CustomerResponse;

public interface GetCustomerUseCase {

    CustomerResponse getById(UUID id);

    CustomerResponse getByCodigo(String codigo);
}
