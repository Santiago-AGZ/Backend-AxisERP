package com.axiserp.report.application.service;

import com.axiserp.report.application.dto.response.InventoryReportResponse;
import com.axiserp.report.ports.input.GenerateInventoryReportUseCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final GenerateInventoryReportUseCase generateInventoryReportUseCase;

    public byte[] exportInventoryReport(UUID categoryId) {
        InventoryReportResponse report = generateInventoryReportUseCase.execute(categoryId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Producto ID", "Producto", "Stock Actual", "Stock Minimo", "Stock Bajo", "Agotado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (var item : report.items()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.productId().toString());
                row.createCell(1).setCellValue(item.productName());
                row.createCell(2).setCellValue(item.currentStock());
                row.createCell(3).setCellValue(item.minStock());
                row.createCell(4).setCellValue(item.lowStock() ? "Si" : "No");
                row.createCell(5).setCellValue(item.depleted() ? "Si" : "No");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
