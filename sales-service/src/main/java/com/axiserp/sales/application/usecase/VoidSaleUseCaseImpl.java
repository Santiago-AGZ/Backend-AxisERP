package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.input.VoidSaleUseCase;
import com.axiserp.sales.ports.output.InventoryServicePort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoidSaleUseCaseImpl implements VoidSaleUseCase {

    private static final Logger log = LoggerFactory.getLogger(VoidSaleUseCaseImpl.class);

    private final SaleRepositoryPort saleRepositoryPort;
    private final InventoryServicePort inventoryServicePort;
    private final AuditService auditService;

    @Override
    @Transactional
    public SaleResponse voidSale(UUID saleId) {
        // 1. Verify ADMIN role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new IllegalStateException("Solo los administradores pueden anular ventas");
        }

        // 2. Load sale and verify status
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        if (sale.getStatus() != SaleStatus.CONFIRMADA && sale.getStatus() != SaleStatus.PAGADA) {
            throw new SaleNotModifiableException("La venta solo puede anularse si esta CONFIRMADA o PAGADA. Estado actual: " + sale.getStatus());
        }

        // 3. Restore stock for each item
        sale.getItems().forEach(item -> {
            try {
                inventoryServicePort.registerReturn(
                        item.getProductId(),
                        item.getQuantity(),
                        "DEVOLUCION_VENTA",
                        sale.getId(),
                        null);
            } catch (Exception e) {
                log.error("stock_return_failed productId={} saleId={} reason={}", item.getProductId(), saleId, e.getMessage());
                throw new RuntimeException("Error al restaurar stock para el producto: " + item.getProductId(), e);
            }
        });

        // 4. Set ANULADA
        sale.setStatus(SaleStatus.ANULADA);
        sale.setUpdatedAt(LocalDateTime.now());

        Sale saved = saleRepositoryPort.save(sale);
        auditService.logSaleVoided(saved.getId(), null, null,
                String.format("saleNumber=%s status=%s", saved.getSaleNumber(), saved.getStatus()));
        log.info("sale_voided id={}", saved.getId());

        return GetSaleUseCaseImpl.toResponse(saved);
    }
}
