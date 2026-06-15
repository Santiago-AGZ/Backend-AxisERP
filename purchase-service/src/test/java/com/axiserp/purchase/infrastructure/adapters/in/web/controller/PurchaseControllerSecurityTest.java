package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

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

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.request.PurchaseItemRequest;
import com.axiserp.purchase.application.dto.request.ReceiveItemRequest;
import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.ports.input.CancelPurchaseUseCase;
import com.axiserp.purchase.ports.input.CreatePurchaseUseCase;
import com.axiserp.purchase.ports.input.GetPurchaseUseCase;
import com.axiserp.purchase.ports.input.ListPurchasesUseCase;
import com.axiserp.purchase.ports.input.ReceivePurchaseUseCase;
import com.axiserp.purchase.ports.input.UpdatePurchaseStatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PurchaseController.class)
@Import(TestSecurityConfig.class)
@DisplayName("PurchaseController Security")
class PurchaseControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreatePurchaseUseCase createPurchaseUseCase;
    @MockitoBean private GetPurchaseUseCase getPurchaseUseCase;
    @MockitoBean private ListPurchasesUseCase listPurchasesUseCase;
    @MockitoBean private UpdatePurchaseStatusUseCase updatePurchaseStatusUseCase;
    @MockitoBean private ReceivePurchaseUseCase receivePurchaseUseCase;
    @MockitoBean private CancelPurchaseUseCase cancelPurchaseUseCase;

    private static final UUID PID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/purchases ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createPurchaseUseCase.execute(any(), any())).thenReturn(PurchaseResponse.builder().id(PID).status(PurchaseStatus.BORRADOR).build());
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePurchaseRequest.builder()
                        .supplierId(PID).items(List.of(PurchaseItemRequest.builder().productId(PID).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/purchases INVENTARIO 201")
    void create_asInventario() throws Exception {
        when(createPurchaseUseCase.execute(any(), any())).thenReturn(PurchaseResponse.builder().id(PID).status(PurchaseStatus.BORRADOR).build());
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePurchaseRequest.builder()
                        .supplierId(PID).items(List.of(PurchaseItemRequest.builder().productId(PID).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/purchases VENDEDOR 403")
    void create_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePurchaseRequest.builder()
                        .supplierId(PID).items(List.of(PurchaseItemRequest.builder().productId(PID).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/purchases UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/purchases").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(CreatePurchaseRequest.builder()
                        .supplierId(PID).items(List.of(PurchaseItemRequest.builder().productId(PID).productName("P").quantity(1).unitPrice(java.math.BigDecimal.TEN).build())).build())))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/purchases/{id} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getPurchaseUseCase.execute(any())).thenReturn(PurchaseResponse.builder().id(PID).build());
        mockMvc.perform(get("/api/v1/purchases/{id}", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/purchases/{id} INVENTARIO 200")
    void get_asInventario() throws Exception {
        when(getPurchaseUseCase.execute(any())).thenReturn(PurchaseResponse.builder().id(PID).build());
        mockMvc.perform(get("/api/v1/purchases/{id}", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/purchases/{id} VENDEDOR 403")
    void get_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/purchases/{id}", PID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/purchases ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listPurchasesUseCase.execute()).thenReturn(List.of());
        when(listPurchasesUseCase.countAll()).thenReturn(0L);
        mockMvc.perform(get("/api/v1/purchases")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/purchases VENDEDOR 403")
    void list_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/purchases")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/purchases/{id}/status ADMIN 200")
    void updateStatus_asAdmin() throws Exception {
        when(updatePurchaseStatusUseCase.execute(any(), any())).thenReturn(PurchaseResponse.builder().id(PID).status(PurchaseStatus.PENDIENTE).build());
        mockMvc.perform(patch("/api/v1/purchases/{id}/status", PID).param("status", "PENDIENTE"))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/purchases/{id}/status INVENTARIO 403")
    void updateStatus_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/purchases/{id}/status", PID).param("status", "PENDIENTE"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/purchases/{id}/receive INVENTARIO 200")
    void receive_asInventario() throws Exception {
        when(receivePurchaseUseCase.execute(any(), any())).thenReturn(PurchaseResponse.builder().id(PID).status(PurchaseStatus.RECIBIDA).build());
        mockMvc.perform(post("/api/v1/purchases/{id}/receive", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ReceivePurchaseRequest.builder()
                        .items(List.of(ReceiveItemRequest.builder().itemId(PID).receivedQuantity(5).build())).build())))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/purchases/{id}/receive VENDEDOR 403")
    void receive_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/purchases/{id}/receive", PID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ReceivePurchaseRequest.builder()
                        .items(List.of(ReceiveItemRequest.builder().itemId(PID).receivedQuantity(1).build())).build())))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/purchases/{id}/cancel ADMIN 200")
    void cancel_asAdmin() throws Exception {
        when(cancelPurchaseUseCase.execute(any())).thenReturn(PurchaseResponse.builder().id(PID).status(PurchaseStatus.CANCELADA).build());
        mockMvc.perform(patch("/api/v1/purchases/{id}/cancel", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PATCH /api/v1/purchases/{id}/cancel INVENTARIO 403")
    void cancel_asInventario() throws Exception {
        mockMvc.perform(patch("/api/v1/purchases/{id}/cancel", PID)).andExpect(status().isForbidden());
    }
}
