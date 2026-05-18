package com.axiserp.catalog.application.usecase;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.catalog.domain.exception.CategoryHasProductsException;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.ports.input.DeleteCategoryUseCase;
import com.axiserp.catalog.ports.output.CategoryRepositoryPort;
import com.axiserp.catalog.ports.output.ProductRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCaseImpl implements DeleteCategoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteCategoryUseCaseImpl.class);

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    @Override
    @Transactional
    public void delete(UUID id) {
        categoryRepositoryPort.findById(id)
                .orElseThrow(CategoryNotFoundException::new);

        int productCount = productRepositoryPort.countByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryHasProductsException(productCount);
        }

        categoryRepositoryPort.deleteById(id);

        log.info("category_deleted id={}", id);
    }
}
