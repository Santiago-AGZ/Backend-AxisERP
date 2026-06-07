package com.axiserp.sales.application.service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.sales.domain.exception.InvoiceNotFoundException;
import com.axiserp.sales.domain.exception.SaleNotFoundException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.Invoice;
import com.axiserp.sales.domain.model.Sale;
import com.axiserp.sales.domain.model.SaleItem;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;
import com.axiserp.sales.ports.output.InvoiceRepositoryPort;
import com.axiserp.sales.ports.output.SaleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private static final Logger log = LoggerFactory.getLogger(CsvExportService.class);

    private final SaleRepositoryPort saleRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;

    public byte[] generateInvoiceCsv(UUID saleId) {
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        Invoice invoice = invoiceRepositoryPort.findBySaleId(saleId)
                .orElseThrow(() -> new InvoiceNotFoundException(saleId));

        Customer customer = customerRepositoryPort.findById(sale.getCustomerId())
                .orElseThrow(() -> new SaleNotFoundException(sale.getCustomerId()));

        StringBuilder sb = new StringBuilder();

        sb.append("FACTURA DE VENTA\n\n");
        sb.append("Factura No,").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("Fecha,").append(invoice.getIssuedAt()).append("\n");
        sb.append("Venta No,").append(sale.getSaleNumber()).append("\n\n");

        sb.append("DATOS DEL CLIENTE\n");
        sb.append("Nombre,").append(customer.getName()).append("\n");
        sb.append("Documento,").append(customer.getDocumentNumber()).append("\n");
        if (customer.getEmail() != null) {
            sb.append("Email,").append(customer.getEmail()).append("\n");
        }
        sb.append("\n");

        sb.append("Producto,Cantidad,P. Unitario,Dto.,Subtotal\n");
        for (SaleItem item : sale.getItems()) {
            sb.append(escapeCsv(item.getProductName())).append(",");
            sb.append(item.getQuantity()).append(",");
            sb.append(item.getUnitPrice()).append(",");
            sb.append(item.getDiscount()).append(",");
            sb.append(item.getSubtotal()).append("\n");
        }

        sb.append("\nRESUMEN\n");
        sb.append("Subtotal,").append(sale.getSubtotal()).append("\n");
        sb.append("Descuento,").append(sale.getDiscount()).append("\n");
        sb.append("IVA (19%),").append(sale.getTax()).append("\n");
        sb.append("Total,").append(sale.getTotal()).append("\n");

        log.info("invoice_csv_generated saleId={}", saleId);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
