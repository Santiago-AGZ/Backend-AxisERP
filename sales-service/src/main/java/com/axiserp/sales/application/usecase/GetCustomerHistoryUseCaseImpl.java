package com.axiserp.sales.application.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.SaleResponseMapper;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.ports.input.GetCustomerHistoryUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCustomerHistoryUseCaseImpl implements GetCustomerHistoryUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> execute(UUID customerId) {
        customerRepositoryPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        UUID createdBy = resolveCreatedByFilter();
        return saleRepositoryPort.findByCustomerId(customerId, createdBy)
                .stream()
                .map(SaleResponseMapper::toResponse)
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
