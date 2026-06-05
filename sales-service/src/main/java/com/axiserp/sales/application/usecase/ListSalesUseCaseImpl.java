package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.ports.input.ListSalesUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListSalesUseCaseImpl implements ListSalesUseCase {

    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> list(UUID customerId, String status, UUID productId, int page, int size) {
        UUID createdBy = resolveCreatedByFilter();
        return saleRepositoryPort.findByFilters(customerId, status, productId, createdBy, page, size)
                .stream()
                .map(GetSaleUseCaseImpl::toResponse)
                .toList();
    }

    private UUID resolveCreatedByFilter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return null;
        if (auth.getPrincipal() instanceof String userIdStr) {
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
