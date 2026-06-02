package com.axiserp.purchase.ports.output;

import java.util.UUID;

public interface CatalogServicePort {
    boolean productExists(UUID productId);
}
