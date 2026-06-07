package com.axiserp.sales.infrastructure.adapters.in.web.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axiserp.sales.application.dto.response.InvoiceResponse;
import com.axiserp.sales.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.sales.ports.input.GenerateInvoiceCsvUseCase;
import com.axiserp.sales.ports.input.GenerateInvoiceExcelUseCase;
import com.axiserp.sales.ports.input.GenerateInvoicePdfUseCase;
import com.axiserp.sales.ports.input.GetInvoiceUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final GetInvoiceUseCase getInvoiceUseCase;
    private final GenerateInvoicePdfUseCase generateInvoicePdfUseCase;
    private final GenerateInvoiceExcelUseCase generateInvoiceExcelUseCase;
    private final GenerateInvoiceCsvUseCase generateInvoiceCsvUseCase;

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(getInvoiceUseCase.getById(id), "Factura encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/by-sale/{saleId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceBySale(@PathVariable UUID saleId) {
        return ResponseEntity.ok(ApiResponse.ok(getInvoiceUseCase.getBySaleId(saleId), "Factura encontrada"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{saleId}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable UUID saleId) {
        byte[] pdf = generateInvoicePdfUseCase.generateInvoicePdf(saleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("factura-" + saleId + ".pdf")
                        .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{saleId}/excel")
    public ResponseEntity<byte[]> generateInvoiceExcel(@PathVariable UUID saleId) {
        byte[] excel = generateInvoiceExcelUseCase.generateInvoiceExcel(saleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("factura-" + saleId + ".xlsx")
                        .build());
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @GetMapping("/{saleId}/csv")
    public ResponseEntity<byte[]> generateInvoiceCsv(@PathVariable UUID saleId) {
        byte[] csv = generateInvoiceCsvUseCase.generateInvoiceCsv(saleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("factura-" + saleId + ".csv")
                        .build());
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
