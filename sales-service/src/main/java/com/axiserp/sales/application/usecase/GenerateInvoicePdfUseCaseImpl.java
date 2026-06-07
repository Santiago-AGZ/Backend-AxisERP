package com.axiserp.sales.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.axiserp.sales.application.service.PdfExportService;
import com.axiserp.sales.ports.input.GenerateInvoicePdfUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateInvoicePdfUseCaseImpl implements GenerateInvoicePdfUseCase {

    private final PdfExportService pdfExportService;

    @Override
    public byte[] generateInvoicePdf(UUID saleId) {
        return pdfExportService.generateInvoicePdf(saleId);
    }
}
