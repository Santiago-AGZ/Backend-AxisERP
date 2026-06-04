package com.axiserp.sales.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.application.service.AuditService;
import com.axiserp.sales.domain.exception.CustomerNotFoundException;
import com.axiserp.sales.domain.exception.InsufficientStockException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotModifiableException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.Invoice;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.input.ConfirmSaleUseCase;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.InventoryServicePort;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfirmSaleUseCaseImpl implements ConfirmSaleUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmSaleUseCaseImpl.class);

    private final SaleRepositoryPort saleRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final InventoryServicePort inventoryServicePort;
    private final AuditService auditService;

    @Override
    @Transactional
    public SaleResponse confirm(UUID saleId) {
        // 1. Load sale and verify it can be confirmed
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        if (sale.getStatus() != SaleStatus.BORRADOR && sale.getStatus() != SaleStatus.PENDIENTE) {
            throw new SaleNotModifiableException("La venta solo puede confirmarse si esta en BORRADOR o PENDIENTE. Estado actual: " + sale.getStatus());
        }

        // 2. Check and exit stock for each item
        for (SaleItem item : sale.getItems()) {
            try {
                inventoryServicePort.checkAndExit(
                        item.getProductId(),
                        item.getQuantity(),
                        "VENTA",
                        sale.getId(),
                        null);
            } catch (InsufficientStockException e) {
                throw e;
            } catch (Exception e) {
                log.error("inventory_exit_failed productId={} saleId={} reason={}", item.getProductId(), saleId, e.getMessage());
                throw new InsufficientStockException("No fue posible descontar stock para el producto: " + item.getProductId() + ". " + e.getMessage());
            }
        }

        // 3. Set status to CONFIRMADA
        sale.setStatus(SaleStatus.CONFIRMADA);
        sale.setUpdatedAt(LocalDateTime.now());
        Sale saved = saleRepositoryPort.save(sale);

        // 4. Create invoice
        Customer customer = customerRepositoryPort.findById(sale.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(sale.getCustomerId()));

        String customerSnapshot = buildCustomerSnapshot(customer);
        String itemsSnapshot = buildItemsSnapshot(sale);

        Invoice invoice = Invoice.builder()
                .saleId(saved.getId())
                .customerSnapshot(customerSnapshot)
                .itemsSnapshot(itemsSnapshot)
                .subtotal(saved.getSubtotal())
                .discount(saved.getDiscount())
                .tax(saved.getTax())
                .total(saved.getTotal())
                .issuedAt(LocalDateTime.now())
                .build();

        Invoice savedInvoice = invoiceRepositoryPort.save(invoice);
        auditService.logSaleConfirmed(saved.getId(), null, null,
                String.format("invoiceNumber=%d total=%s", savedInvoice.getInvoiceNumber(), saved.getTotal()));
        log.info("sale_confirmed id={} invoiceNumber={}", saved.getId(), savedInvoice.getInvoiceNumber());

        return GetSaleUseCaseImpl.toResponse(saved);
    }

    private String buildCustomerSnapshot(Customer c) {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"documentNumber\":\"%s\",\"email\":\"%s\"}",
                c.getId(), c.getName(), c.getDocumentNumber(), c.getEmail() != null ? c.getEmail() : "");
    }

    private String buildItemsSnapshot(Sale sale) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < sale.getItems().size(); i++) {
            SaleItem item = sale.getItems().get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                    "{\"productId\":\"%s\",\"productName\":\"%s\",\"quantity\":%d,\"unitPrice\":%s,\"discount\":%s,\"subtotal\":%s}",
                    item.getProductId(),
                    item.getProductName() != null ? item.getProductName().replace("\"", "\\\"") : "",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getDiscount(),
                    item.getSubtotal()));
        }
        sb.append("]");
        return sb.toString();
    }
}
