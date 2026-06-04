package com.axiserp.report.ports.input;

import com.axiserp.report.application.dto.response.InventoryReportResponse;

import java.util.UUID;

public interface GenerateInventoryReportUseCase {
    InventoryReportResponse execute(UUID categoryId);
}
