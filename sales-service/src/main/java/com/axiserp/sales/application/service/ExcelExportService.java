package com.axiserp.sales.application.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
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
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');

            writer.write("FACTURA DE VENTA\n");
            writer.write("Factura No," + String.valueOf(invoice.getInvoiceNumber()) + "\n");
            writer.write("Fecha," + String.valueOf(invoice.getIssuedAt()) + "\n");
            writer.write("Venta No," + sale.getSaleNumber() + "\n");
            writer.write("\n");

            writer.write("DATOS DEL CLIENTE\n");
            writer.write("Nombre," + customer.getName() + "\n");
            writer.write("Documento," + customer.getDocumentNumber() + "\n");
            if (customer.getEmail() != null) {
                writer.write("Email," + customer.getEmail() + "\n");
            }
            writer.write("\n");

            writer.write("Producto,Cantidad,P. Unitario,Dto.,Subtotal\n");
            for (SaleItem item : sale.getItems()) {
                writer.write(escapeCsv(item.getProductName() != null ? item.getProductName() : "") + ",");
                writer.write(String.valueOf(item.getQuantity()) + ",");
                writer.write(formatPrice(item.getUnitPrice()) + ",");
                writer.write(formatPrice(item.getDiscount()) + ",");
                writer.write(formatPrice(item.getSubtotal()) + "\n");
            }
            writer.write("\n");

            writer.write("RESUMEN\n");
            writer.write("Subtotal," + formatPrice(sale.getSubtotal()) + "\n");
            writer.write("Descuento," + formatPrice(sale.getDiscount()) + "\n");
            writer.write("IVA (19%)," + formatPrice(sale.getTax()) + "\n");
            writer.write("Total," + formatPrice(sale.getTotal()) + "\n");

            writer.flush();
        } catch (Exception e) {
            log.error("Error generating CSV for saleId={}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Error al generar el CSV de la factura", e);
        }

        log.info("invoice_csv_generated saleId={}", saleId);
        return baos.toByteArray();
    }

    private String formatPrice(BigDecimal price) {
        return price != null ? price.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}