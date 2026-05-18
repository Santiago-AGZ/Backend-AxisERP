package com.axiserp.catalog.ports.input;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void delete(UUID id);
}
