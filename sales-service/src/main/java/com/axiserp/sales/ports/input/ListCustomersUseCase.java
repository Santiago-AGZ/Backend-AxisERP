package com.axiserp.sales.ports.input;

import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;

public interface ListCustomersUseCase {

    PaginatedResponse<CustomerResponse> list(String search, boolean includeInactive, int page, int size);
}
