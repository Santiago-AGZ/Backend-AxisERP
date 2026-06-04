package com.axiserp.sales.application.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);

    private final SaleRepositoryPort saleRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;

    public byte[] generateInvoicePdf(UUID saleId) {
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        Invoice invoice = invoiceRepositoryPort.findBySaleId(saleId)
                .orElseThrow(() -> new InvoiceNotFoundException(saleId));

        Customer customer = customerRepositoryPort.findById(sale.getCustomerId())
                .orElseThrow(() -> new SaleNotFoundException(sale.getCustomerId()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(com.lowagie.text.PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("FACTURA DE VENTA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Factura No: " + invoice.getInvoiceNumber(), headerFont));
            document.add(new Paragraph("Fecha: " + invoice.getIssuedAt(), normalFont));
            document.add(new Paragraph("Venta No: " + sale.getSaleNumber(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("DATOS DEL CLIENTE", headerFont));
            document.add(new Paragraph("Nombre: " + customer.getName(), normalFont));
            document.add(new Paragraph("Documento: " + customer.getDocumentNumber(), normalFont));
            if (customer.getEmail() != null) {
                document.add(new Paragraph("Email: " + customer.getEmail(), normalFont));
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("DETALLE DE PRODUCTOS", headerFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 1.5f, 1f, 1.5f, 1.5f});

            String[] headers = {"Producto", "Cantidad", "P. Unitario", "Dto.", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (SaleItem item : sale.getItems()) {
                table.addCell(new Phrase(item.getProductName() != null ? item.getProductName() : "", normalFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                table.addCell(new Phrase("$ " + item.getUnitPrice(), normalFont));
                table.addCell(new Phrase("$ " + item.getDiscount(), normalFont));
                table.addCell(new Phrase("$ " + item.getSubtotal(), normalFont));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("RESUMEN", headerFont));
            document.add(new Paragraph("Subtotal: $ " + sale.getSubtotal(), normalFont));
            document.add(new Paragraph("Descuento: $ " + sale.getDiscount(), normalFont));
            document.add(new Paragraph("IVA (19%): $ " + sale.getTax(), normalFont));
            document.add(new Paragraph("Total: $ " + sale.getTotal(), headerFont));

            document.close();
        } catch (DocumentException e) {
            log.error("Error generating PDF for saleId={}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF de la factura", e);
        }

        return baos.toByteArray();
    }
}
