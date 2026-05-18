package com.axiserp.catalog.ports.input;

import java.util.List;

import com.axiserp.catalog.application.dto.response.CategoryResponse;

public interface ListCategoriesUseCase {
    List<CategoryResponse> listAll();
}
