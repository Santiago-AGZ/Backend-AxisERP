package com.axiserp.catalog.ports.output;

import java.util.UUID;

public interface SalesServicePort {
    boolean hasPendingSales(UUID productId);
}
