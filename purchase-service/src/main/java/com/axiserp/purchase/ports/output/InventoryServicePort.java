package com.axiserp.purchase.ports.output;

import java.util.UUID;

public interface InventoryServicePort {
    void registerEntry(UUID productId, int quantity, String referenceType, UUID referenceId, String notes);
    void reverseMovements(UUID purchaseId);
}
