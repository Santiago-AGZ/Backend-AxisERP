package com.axiserp.report.application.service;

import com.axiserp.report.application.dto.response.SalesReportResponse;
import com.axiserp.report.ports.input.GenerateSalesReportUseCase;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final GenerateSalesReportUseCase generateSalesReportUseCase;

    public byte[] exportSalesReport(LocalDate startDate, LocalDate endDate, UUID clientId) {
        SalesReportResponse report = generateSalesReportUseCase.execute(startDate, endDate, null, null, clientId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

        Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        String rangeText = (startDate != null ? startDate.toString() : "inicio")
                + " a " + (endDate != null ? endDate.toString() : "hoy");
        Paragraph subtitle = new Paragraph("Periodo: " + rangeText, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        document.add(Chunk.NEWLINE);

        Paragraph summary = new Paragraph(
                "Total Ventas: " + report.totalSales()
                + " | Ingresos: $" + report.totalRevenue()
                + " | Impuestos: $" + report.totalTax()
                + " | Descuentos: $" + report.totalDiscount(),
                FontFactory.getFont(FontFactory.HELVETICA, 11)
        );
        document.add(summary);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 3, 1.5f, 2, 2});

        String[] headers = {"Numero", "Estado", "Items", "Total", "Fecha"};
        for (String head : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(head, headerFont));
            cell.setBackgroundColor(new Color(70, 130, 180));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (var sale : report.recentSales()) {
            table.addCell(new PdfPCell(new Phrase(sale.saleNumber(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(sale.status(), cellFont)));
            table.addCell(new PdfPCell(new Phrase("1", cellFont)));
            table.addCell(new PdfPCell(new Phrase("$" + sale.total().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(sale.createdAt() != null ? sale.createdAt().toLocalDate().toString() : "", cellFont)));
        }

        document.add(table);

        document.add(Chunk.NEWLINE);

        Paragraph footer = new Paragraph(
                "Generado el: " + LocalDate.now(),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.LIGHT_GRAY)
        );
        document.add(footer);

        document.close();

        return baos.toByteArray();
    }
}
