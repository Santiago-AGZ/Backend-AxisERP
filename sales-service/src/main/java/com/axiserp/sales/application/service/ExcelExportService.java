package com.axiserp.sales.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    public byte[] generateInvoiceExcel(UUID saleId) {
        Sale sale = saleRepositoryPort.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        Invoice invoice = invoiceRepositoryPort.findBySaleId(saleId)
                .orElseThrow(() -> new InvoiceNotFoundException(saleId));

        Customer customer = customerRepositoryPort.findById(sale.getCustomerId())
                .orElseThrow(() -> new SaleNotFoundException(sale.getCustomerId()));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Factura");

            CellStyle titleStyle = createStyle(workbook, true, (short) 16);
            CellStyle headerStyle = createStyle(workbook, true, (short) 11);
            CellStyle labelStyle = createStyle(workbook, true, (short) 11);
            CellStyle valueStyle = createStyle(workbook, false, (short) 11);
            CellStyle priceStyle = createStyle(workbook, false, (short) 11);
            priceStyle.setAlignment(HorizontalAlignment.RIGHT);

            int r = 0;

            createRow(sheet, r++, titleStyle, "FACTURA DE VENTA");

            r++;
            createRow(sheet, r++, labelStyle, "Factura No:", String.valueOf(invoice.getInvoiceNumber()));
            createRow(sheet, r++, labelStyle, "Fecha:", String.valueOf(invoice.getIssuedAt()));
            createRow(sheet, r++, labelStyle, "Venta No:", sale.getSaleNumber());

            r++;
            createRow(sheet, r++, headerStyle, "DATOS DEL CLIENTE");
            createRow(sheet, r++, labelStyle, "Nombre:", customer.getName());
            createRow(sheet, r++, labelStyle, "Documento:", customer.getDocumentNumber());
            if (customer.getEmail() != null) {
                createRow(sheet, r++, labelStyle, "Email:", customer.getEmail());
            }

            r++;
            Row headerRow = sheet.createRow(r++);
            String[] headers = {"Producto", "Cantidad", "P. Unitario", "Dto.", "Subtotal"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (SaleItem item : sale.getItems()) {
                Row dataRow = sheet.createRow(r++);
                dataRow.createCell(0).setCellValue(item.getProductName() != null ? item.getProductName() : "");
                dataRow.createCell(1).setCellValue(item.getQuantity());

                Cell priceCell = dataRow.createCell(2);
                priceCell.setCellValue(formatPrice(item.getUnitPrice()));
                priceCell.setCellStyle(priceStyle);

                Cell discCell = dataRow.createCell(3);
                discCell.setCellValue(formatPrice(item.getDiscount()));
                discCell.setCellStyle(priceStyle);

                Cell subCell = dataRow.createCell(4);
                subCell.setCellValue(formatPrice(item.getSubtotal()));
                subCell.setCellStyle(priceStyle);
            }

            r++;
            createRow(sheet, r++, headerStyle, "RESUMEN");
            createRow(sheet, r++, labelStyle, "Subtotal:", formatPrice(sale.getSubtotal()));
            createRow(sheet, r++, labelStyle, "Descuento:", formatPrice(sale.getDiscount()));
            createRow(sheet, r++, labelStyle, "IVA (19%):", formatPrice(sale.getTax()));
            createRow(sheet, r++, labelStyle, "Total:", formatPrice(sale.getTotal()));

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (var baos = new java.io.ByteArrayOutputStream()) {
                workbook.write(baos);
                log.info("invoice_excel_generated saleId={}", saleId);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error generating Excel for saleId={}: {}", saleId, e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel de la factura", e);
        }
    }

    private String formatPrice(BigDecimal price) {
        return price != null ? price.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
    }

    private CellStyle createStyle(Workbook wb, boolean bold, short fontSize) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);
        return style;
    }

    private void createRow(Sheet sheet, int rowNum, CellStyle style, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            cell.setCellStyle(style);
        }
    }
}