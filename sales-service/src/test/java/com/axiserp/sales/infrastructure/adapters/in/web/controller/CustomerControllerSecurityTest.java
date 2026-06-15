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

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.application.dto.request.UpdateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.ports.input.CreateCustomerUseCase;
import com.axiserp.sales.ports.input.DeactivateCustomerUseCase;
import com.axiserp.sales.ports.input.GetCustomerHistoryUseCase;
import com.axiserp.sales.ports.input.GetCustomerUseCase;
import com.axiserp.sales.ports.input.ListCustomersUseCase;
import com.axiserp.sales.ports.input.ReactivateCustomerUseCase;
import com.axiserp.sales.ports.input.UpdateCustomerUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CustomerController.class)
@Import(TestSecurityConfig.class)
@DisplayName("CustomerController Security")
class CustomerControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateCustomerUseCase createCustomerUseCase;
    @MockitoBean private GetCustomerUseCase getCustomerUseCase;
    @MockitoBean private ListCustomersUseCase listCustomersUseCase;
    @MockitoBean private DeactivateCustomerUseCase deactivateCustomerUseCase;
    @MockitoBean private ReactivateCustomerUseCase reactivateCustomerUseCase;
    @MockitoBean private UpdateCustomerUseCase updateCustomerUseCase;
    @MockitoBean private GetCustomerHistoryUseCase getCustomerHistoryUseCase;

    private static final UUID CID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/customers ADMIN 201")
    void create_asAdmin() throws Exception {
        when(createCustomerUseCase.create(any(), any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCustomerRequest("C001","N","DNI","12345678","e@e.com","555","addr"))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/customers VENDEDOR 201")
    void create_asVendedor() throws Exception {
        when(createCustomerUseCase.create(any(), any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCustomerRequest("C001","N","DNI","12345678","e@e.com","555","addr"))))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/customers INVENTARIO 403")
    void create_asInventario() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCustomerRequest("C001","N","DNI","12345678","e@e.com","555","addr"))))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/customers UNAUTH 401")
    void create_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateCustomerRequest("C001","N","DNI","12345678","e@e.com","555","addr"))))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/customers/{codigo} ADMIN 200")
    void get_asAdmin() throws Exception {
        when(getCustomerUseCase.getByCodigo(any())).thenReturn(CustomerResponse.builder().codigo("C001").build());
        mockMvc.perform(get("/api/v1/customers/{codigo}", "C001")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/customers/{codigo} VENDEDOR 200")
    void get_asVendedor() throws Exception {
        when(getCustomerUseCase.getByCodigo(any())).thenReturn(CustomerResponse.builder().codigo("C001").build());
        mockMvc.perform(get("/api/v1/customers/{codigo}", "C001")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/customers/{codigo} UNAUTH 401")
    void get_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{codigo}", "C001")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/customers ADMIN 200")
    void list_asAdmin() throws Exception {
        when(listCustomersUseCase.list(any(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(PaginatedResponse.<CustomerResponse>builder().content(List.of()).totalRecords(0).build());
        mockMvc.perform(get("/api/v1/customers")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/customers VENDEDOR 200")
    void list_asVendedor() throws Exception {
        when(listCustomersUseCase.list(any(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(PaginatedResponse.<CustomerResponse>builder().content(List.of()).totalRecords(0).build());
        mockMvc.perform(get("/api/v1/customers")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/customers UNAUTH 401")
    void list_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/customers")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/customers/{id}/deactivate ADMIN 200")
    void deactivate_asAdmin() throws Exception {
        when(deactivateCustomerUseCase.deactivate(any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(patch("/api/v1/customers/{id}/deactivate", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PATCH /api/v1/customers/{id}/deactivate VENDEDOR 403")
    void deactivate_asVendedor() throws Exception {
        mockMvc.perform(patch("/api/v1/customers/{id}/deactivate", CID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PATCH /api/v1/customers/{id}/reactivate ADMIN 200")
    void reactivate_asAdmin() throws Exception {
        when(reactivateCustomerUseCase.reactivate(any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(patch("/api/v1/customers/{id}/reactivate", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PATCH /api/v1/customers/{id}/reactivate VENDEDOR 403")
    void reactivate_asVendedor() throws Exception {
        mockMvc.perform(patch("/api/v1/customers/{id}/reactivate", CID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("PUT /api/v1/customers/{id} ADMIN 200")
    void update_asAdmin() throws Exception {
        when(updateCustomerUseCase.execute(any(), any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(put("/api/v1/customers/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCustomerRequest())))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("PUT /api/v1/customers/{id} VENDEDOR 200")
    void update_asVendedor() throws Exception {
        when(updateCustomerUseCase.execute(any(), any())).thenReturn(CustomerResponse.builder().id(CID).build());
        mockMvc.perform(put("/api/v1/customers/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCustomerRequest())))
            .andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("PUT /api/v1/customers/{id} INVENTARIO 403")
    void update_asInventario() throws Exception {
        mockMvc.perform(put("/api/v1/customers/{id}", CID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateCustomerRequest("U","e@e.com","555","addr"))))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/customers/{id}/history ADMIN 200")
    void history_asAdmin() throws Exception {
        when(getCustomerHistoryUseCase.execute(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/customers/{customerId}/history", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/customers/{id}/history VENDEDOR 200")
    void history_asVendedor() throws Exception {
        when(getCustomerHistoryUseCase.execute(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/customers/{customerId}/history", CID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/customers/{id}/history INVENTARIO 403")
    void history_asInventario() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{customerId}/history", CID)).andExpect(status().isForbidden());
    }
}
