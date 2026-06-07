package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.service.ExcelExportService;
import com.axiserp.sales.ports.input.GenerateInvoiceExcelUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateInvoiceExcelUseCaseImpl implements GenerateInvoiceExcelUseCase {

    private final ExcelExportService excelExportService;

    @Override
    public byte[] generateInvoiceExcel(UUID saleId) {
        return excelExportService.generateInvoiceExcel(saleId);
    }
}
