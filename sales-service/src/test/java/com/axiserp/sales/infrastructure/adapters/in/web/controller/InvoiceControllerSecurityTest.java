package com.axiserp.sales.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.sales.application.dto.response.InvoiceResponse;
import com.axiserp.sales.ports.input.GenerateInvoiceCsvUseCase;
import com.axiserp.sales.ports.input.GenerateInvoiceExcelUseCase;
import com.axiserp.sales.ports.input.GenerateInvoicePdfUseCase;
import com.axiserp.sales.ports.input.GetInvoiceUseCase;

@WebMvcTest(controllers = InvoiceController.class)
@Import(TestSecurityConfig.class)
@DisplayName("InvoiceController Security")
class InvoiceControllerSecurityTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private GetInvoiceUseCase getInvoiceUseCase;
    @MockitoBean private GenerateInvoicePdfUseCase generateInvoicePdfUseCase;
    @MockitoBean private GenerateInvoiceExcelUseCase generateInvoiceExcelUseCase;
    @MockitoBean private GenerateInvoiceCsvUseCase generateInvoiceCsvUseCase;

    private static final UUID IID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID SID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/invoices/{id} ADMIN 200")
    void getInvoice_asAdmin() throws Exception {
        when(getInvoiceUseCase.getById(any())).thenReturn(InvoiceResponse.builder().id(IID).build());
        mockMvc.perform(get("/api/v1/invoices/{id}", IID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/invoices/{id} VENDEDOR 200")
    void getInvoice_asVendedor() throws Exception {
        when(getInvoiceUseCase.getById(any())).thenReturn(InvoiceResponse.builder().id(IID).build());
        mockMvc.perform(get("/api/v1/invoices/{id}", IID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/invoices/{id} INVENTARIO 403")
    void getInvoice_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{id}", IID)).andExpect(status().isForbidden());
    }

    @Test @DisplayName("GET /api/v1/invoices/{id} UNAUTH 401")
    void getInvoice_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{id}", IID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/invoices/by-sale/{saleId} ADMIN 200")
    void getBySale_asAdmin() throws Exception {
        when(getInvoiceUseCase.getBySaleId(any())).thenReturn(InvoiceResponse.builder().id(IID).build());
        mockMvc.perform(get("/api/v1/invoices/by-sale/{saleId}", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/invoices/by-sale/{saleId} VENDEDOR 200")
    void getBySale_asVendedor() throws Exception {
        when(getInvoiceUseCase.getBySaleId(any())).thenReturn(InvoiceResponse.builder().id(IID).build());
        mockMvc.perform(get("/api/v1/invoices/by-sale/{saleId}", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/invoices/by-sale/{saleId} INVENTARIO 403")
    void getBySale_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/by-sale/{saleId}", SID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/invoices/{saleId}/pdf ADMIN 200")
    void pdf_asAdmin() throws Exception {
        when(generateInvoicePdfUseCase.generateInvoicePdf(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/pdf", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/invoices/{saleId}/pdf VENDEDOR 200")
    void pdf_asVendedor() throws Exception {
        when(generateInvoicePdfUseCase.generateInvoicePdf(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/pdf", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/invoices/{saleId}/pdf INVENTARIO 403")
    void pdf_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{saleId}/pdf", SID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/invoices/{saleId}/excel ADMIN 200")
    void excel_asAdmin() throws Exception {
        when(generateInvoiceExcelUseCase.generateInvoiceExcel(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/excel", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/invoices/{saleId}/excel VENDEDOR 200")
    void excel_asVendedor() throws Exception {
        when(generateInvoiceExcelUseCase.generateInvoiceExcel(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/excel", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/invoices/{saleId}/excel INVENTARIO 403")
    void excel_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{saleId}/excel", SID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/invoices/{saleId}/csv ADMIN 200")
    void csv_asAdmin() throws Exception {
        when(generateInvoiceCsvUseCase.generateInvoiceCsv(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/csv", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/invoices/{saleId}/csv VENDEDOR 200")
    void csv_asVendedor() throws Exception {
        when(generateInvoiceCsvUseCase.generateInvoiceCsv(any())).thenReturn(new byte[]{1,2,3});
        mockMvc.perform(get("/api/v1/invoices/{saleId}/csv", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/invoices/{saleId}/csv INVENTARIO 403")
    void csv_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/{saleId}/csv", SID)).andExpect(status().isForbidden());
    }
}
