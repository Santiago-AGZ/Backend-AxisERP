package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.service.CsvExportService;
import com.axiserp.sales.ports.input.GenerateInvoiceCsvUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateInvoiceCsvUseCaseImpl implements GenerateInvoiceCsvUseCase {

    private final CsvExportService csvExportService;

    @Override
    public byte[] generateInvoiceCsv(UUID saleId) {
        return csvExportService.generateInvoiceCsv(saleId);
    }
}
