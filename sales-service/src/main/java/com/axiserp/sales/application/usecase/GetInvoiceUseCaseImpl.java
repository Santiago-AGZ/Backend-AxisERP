package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.dto.response.InvoiceResponse;
import com.axiserp.sales.domain.exception.InvoiceNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.model.Invoice;
import com.axiserp.sales.ports.input.GetInvoiceUseCase;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInvoiceUseCaseImpl implements GetInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;

    @Override
    public InvoiceResponse getById(UUID id) {
        Invoice invoice = invoiceRepositoryPort.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        checkSaleOwnership(invoice.getSaleId());
        return toResponse(invoice);
    }

    @Override
    public InvoiceResponse getBySaleId(UUID saleId) {
        checkSaleOwnership(saleId);
        Invoice invoice = invoiceRepositoryPort.findBySaleId(saleId)
                .orElseThrow(() -> new InvoiceNotFoundException("No se encontro factura para la venta: " + saleId));
        return toResponse(invoice);
    }

    private void checkSaleOwnership(UUID saleId) {
        var sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));
        GetSaleUseCaseImpl.checkOwnership(sale);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .saleId(invoice.getSaleId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerSnapshot(invoice.getCustomerSnapshot())
                .itemsSnapshot(invoice.getItemsSnapshot())
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .issuedAt(invoice.getIssuedAt())
                .build();
    }
}
