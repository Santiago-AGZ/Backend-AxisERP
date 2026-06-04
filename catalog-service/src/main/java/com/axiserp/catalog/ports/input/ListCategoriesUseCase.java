package com.axiserp.catalog.ports.input;

import java.util.List;

import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface ListCategoriesUseCase {
    List<CategoryResponse> listAll();
    List<CategoryResponse> listAll(int page, int size);
    List<CategoryResponse> findByFilters(String search, boolean includeInactive, int page, int size);
    long countByFilters(String search, boolean includeInactive);
    long countAll();
}
