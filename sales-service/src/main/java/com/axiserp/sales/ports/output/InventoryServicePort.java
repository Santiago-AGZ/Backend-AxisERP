package com.axiserp.sales.ports.output;

import java.util.UUID;

public interface InventoryServicePort {

    void checkAndExit(UUID productId, int quantity, String referenceType, UUID referenceId, String notes);

    void registerReturn(UUID productId, int quantity, String referenceType, UUID referenceId, String notes);
}
