package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.SaleResponseMapper;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.exception.SaleAccessDeniedException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.ports.input.GetSaleUseCase;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSaleUseCaseImpl implements GetSaleUseCase {

    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(UUID id) {
        Sale sale = saleRepositoryPort.findById(id)
                .orElseThrow(() -> new SaleNotFoundException(id));
        checkOwnership(sale);
        return SaleResponseMapper.toResponse(sale);
    }

    static void checkOwnership(Sale sale) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;
        if (auth.getPrincipal() instanceof String userIdStr) {
            try {
                UUID currentUserId = UUID.fromString(userIdStr);
                if (sale.getCreatedBy() != null && !sale.getCreatedBy().equals(currentUserId)) {
                    throw new SaleAccessDeniedException(sale.getId());
                }
            } catch (IllegalArgumentException e) {
                throw new SaleAccessDeniedException(sale.getId());
            }
        }
    }

}
