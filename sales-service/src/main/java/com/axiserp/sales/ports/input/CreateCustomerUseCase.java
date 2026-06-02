package com.axiserp.sales.ports.input;

import java.util.UUID;

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;

public interface CreateCustomerUseCase {

    CustomerResponse create(CreateCustomerRequest request, UUID createdBy);
}
