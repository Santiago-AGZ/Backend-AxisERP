package com.axiserp.sales.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.axiserp.sales.application.dto.request.CreateSaleRequest;
import com.axiserp.sales.application.dto.request.SaleItemRequest;
import com.axiserp.sales.application.dto.response.AuditLogResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.ports.input.ConfirmSaleUseCase;
import com.axiserp.sales.ports.input.CreateSaleUseCase;
import com.axiserp.sales.ports.input.GetSaleUseCase;
import com.axiserp.sales.ports.input.ListSalesUseCase;
import com.axiserp.sales.ports.input.PaySaleUseCase;
import com.axiserp.sales.ports.input.VoidSaleUseCase;
import com.axiserp.sales.ports.input.ListAuditLogsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = SaleController.class)
@Import(TestSecurityConfig.class)
@DisplayName("SaleController Security")
class SaleControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateSaleUseCase createSaleUseCase;
    @MockitoBean private GetSaleUseCase getSaleUseCase;
    @MockitoBean private ListSalesUseCase listSalesUseCase;
    @MockitoBean private ConfirmSaleUseCase confirmSaleUseCase;
    @MockitoBean private PaySaleUseCase paySaleUseCase;
    @MockitoBean private VoidSaleUseCase voidSaleUseCase;
    @MockitoBean private ListAuditLogsUseCase listAuditLogsUseCase;

    private static final UUID SID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/sales ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createSaleUseCase.create(any(), any(), anyBoolean())).thenReturn(SaleResponse.builder().id(SID).build());
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSaleRequest.builder().customerId(UUID.randomUUID()).items(List.of(SaleItemRequest.builder().productId(UUID.randomUUID()).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/sales VENDEDOR 201")
    void create_asVendedor() throws Exception {
        when(createSaleUseCase.create(any(), any(), anyBoolean())).thenReturn(SaleResponse.builder().id(SID).build());
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSaleRequest.builder().customerId(UUID.randomUUID()).items(List.of(SaleItemRequest.builder().productId(UUID.randomUUID()).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/sales INVENTARIO 403")
    void create_asInventario() throws Exception {
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSaleRequest.builder().customerId(UUID.randomUUID()).items(List.of(SaleItemRequest.builder().productId(UUID.randomUUID()).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/sales UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/sales").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreateSaleRequest.builder().customerId(UUID.randomUUID()).items(List.of(SaleItemRequest.builder().productId(UUID.randomUUID()).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/sales/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getSaleUseCase.getById(any())).thenReturn(SaleResponse.builder().id(SID).build());
        mockMvc.perform(get("/api/v1/sales/{id}", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/sales/{id} VENDEDOR 200")
    void get_asVendedor() throws Exception {
        when(getSaleUseCase.getById(any())).thenReturn(SaleResponse.builder().id(SID).build());
        mockMvc.perform(get("/api/v1/sales/{id}", SID)).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/sales/{id} UNAUTH 401")
    void get_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/sales/{id}", SID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/sales ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listSalesUseCase.list(any(), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(PaginatedResponse.<SaleResponse>builder().content(List.of()).totalRecords(0).build());
        mockMvc.perform(get("/api/v1/sales")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/sales VENDEDOR 200")
    void list_asVendedor() throws Exception {
        when(listSalesUseCase.list(any(), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(PaginatedResponse.<SaleResponse>builder().content(List.of()).totalRecords(0).build());
        mockMvc.perform(get("/api/v1/sales")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/sales UNAUTH 401")
    void list_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/sales")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/sales/{id}/confirm ADMIN 200")
    void confirm_asAdmin() throws Exception {
        when(confirmSaleUseCase.confirm(any())).thenReturn(SaleResponse.builder().id(SID).status(SaleStatus.CONFIRMADA.name()).build());
        mockMvc.perform(patch("/api/v1/sales/{id}/confirm", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PATCH /api/v1/sales/{id}/confirm VENDEDOR 200")
    void confirm_asVendedor() throws Exception {
        when(confirmSaleUseCase.confirm(any())).thenReturn(SaleResponse.builder().id(SID).status(SaleStatus.CONFIRMADA.name()).build());
        mockMvc.perform(patch("/api/v1/sales/{id}/confirm", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/sales/{id}/confirm INVENTARIO 403")
    void confirm_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/sales/{id}/confirm", SID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/sales/{id}/pay ADMIN 200")
    void pay_asAdmin() throws Exception {
        when(paySaleUseCase.pay(any())).thenReturn(SaleResponse.builder().id(SID).status(SaleStatus.PAGADA.name()).build());
        mockMvc.perform(patch("/api/v1/sales/{id}/pay", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PATCH /api/v1/sales/{id}/pay VENDEDOR 200")
    void pay_asVendedor() throws Exception {
        when(paySaleUseCase.pay(any())).thenReturn(SaleResponse.builder().id(SID).status(SaleStatus.PAGADA.name()).build());
        mockMvc.perform(patch("/api/v1/sales/{id}/pay", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/sales/{id}/void ADMIN 200")
    void voidSale_asAdmin() throws Exception {
        when(voidSaleUseCase.voidSale(any())).thenReturn(SaleResponse.builder().id(SID).status(SaleStatus.ANULADA.name()).build());
        mockMvc.perform(patch("/api/v1/sales/{id}/void", SID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PATCH /api/v1/sales/{id}/void VENDEDOR 403")
    void voidSale_asVendedor() throws Exception {
        mockMvc.perform(patch("/api/v1/sales/{id}/void", SID)).andExpect(status().isForbidden());
    }

    @Test @DisplayName("PATCH /api/v1/sales/{id}/void UNAUTH 401")
    void voidSale_unauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/sales/{id}/void", SID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/sales/audit-log ADMIN 200")
    void auditLog_asAdmin() throws Exception {
        when(listAuditLogsUseCase.list(anyInt(), anyInt()))
            .thenReturn(PaginatedResponse.<AuditLogResponse>builder().content(List.of()).totalRecords(0).build());
        mockMvc.perform(get("/api/v1/sales/audit-log")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/sales/audit-log VENDEDOR 403")
    void auditLog_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/sales/audit-log")).andExpect(status().isForbidden());
    }
}
