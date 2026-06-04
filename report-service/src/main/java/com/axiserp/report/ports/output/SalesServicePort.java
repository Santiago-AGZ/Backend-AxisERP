package com.axiserp.report.ports.output;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.UUID;

public interface SalesServicePort {
    JsonNode fetchSales(LocalDate startDate, LocalDate endDate, String status, UUID userId, UUID clientId, int page, int size);

    JsonNode fetchCustomers(String search, boolean includeInactive, int page, int size);

    JsonNode fetchCustomerById(UUID customerId);

    JsonNode fetchSalesByIds(String saleIds);
}
