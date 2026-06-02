package com.axiserp.sales.ports.input;

import java.util.List;

import com.axiserp.sales.application.dto.response.CustomerResponse;

public interface ListCustomersUseCase {

    List<CustomerResponse> list(String search, boolean includeInactive, int page, int size);
}
