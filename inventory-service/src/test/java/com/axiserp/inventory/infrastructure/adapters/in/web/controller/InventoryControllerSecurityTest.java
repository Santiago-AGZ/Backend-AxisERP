package com.axiserp.inventory.infrastructure.adapters.in.web.controller;

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

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.dto.response.ProductInventoryResponse;
import com.axiserp.inventory.application.shared.PageResult;
import com.axiserp.inventory.ports.input.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = InventoryController.class)
@Import(TestSecurityConfig.class)
@DisplayName("InventoryController Security")
class InventoryControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private InitializeInventoryUseCase initializeInventoryUseCase;
    @MockitoBean private ListProductsUseCase listProductsUseCase;
    @MockitoBean private GetLowStockAlertsUseCase getLowStockAlertsUseCase;
    @MockitoBean private GetDepletedAlertsUseCase getDepletedAlertsUseCase;
    @MockitoBean private GetInventoryUseCase getInventoryUseCase;
    @MockitoBean private ListMovementsUseCase listMovementsUseCase;
    @MockitoBean private RegisterEntryUseCase registerEntryUseCase;
    @MockitoBean private RegisterExitUseCase registerExitUseCase;
    @MockitoBean private RegisterReturnUseCase registerReturnUseCase;
    @MockitoBean private RegisterAdjustmentUseCase registerAdjustmentUseCase;
    @MockitoBean private ReverseMovementUseCase reverseMovementUseCase;

    private static final UUID PID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID MID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/inventory/initialize ADMIN 201")
    void initialize_asAdmin() throws Exception {
        when(initializeInventoryUseCase.initialize(any(), any())).thenReturn(InventoryResponse.builder().id(PID).currentStock(100).build());
        mockMvc.perform(post("/api/v1/inventory/initialize").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(InitializeInventoryRequest.builder().productId(PID).initialStock(100).minStock(10).maxStock(200).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/inventory/initialize INVENTARIO 201")
    void initialize_asInventario() throws Exception {
        when(initializeInventoryUseCase.initialize(any(), any())).thenReturn(InventoryResponse.builder().id(PID).currentStock(100).build());
        mockMvc.perform(post("/api/v1/inventory/initialize").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(InitializeInventoryRequest.builder().productId(PID).initialStock(100).minStock(10).maxStock(200).build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/inventory/initialize VENDEDOR 403")
    void initialize_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/initialize").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(InitializeInventoryRequest.builder().productId(PID).initialStock(100).minStock(10).maxStock(200).build())))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/inventory/initialize UNAUTH 401")
    void initialize_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/initialize").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(InitializeInventoryRequest.builder().productId(PID).initialStock(100).minStock(10).maxStock(200).build())))
            .andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/inventory/products ADMIN 200")
    void listProducts_asAdmin() throws Exception {
        when(listProductsUseCase.list(anyInt(), anyInt(), any())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/products")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/inventory/products VENDEDOR 200")
    void listProducts_asVendedor() throws Exception {
        when(listProductsUseCase.list(anyInt(), anyInt(), any())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/products")).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/inventory/products UNAUTH 401")
    void listProducts_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/products")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/inventory/alerts ADMIN 200")
    void alerts_asAdmin() throws Exception {
        when(getLowStockAlertsUseCase.execute(anyInt(), anyInt())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/alerts")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/inventory/alerts INVENTARIO 200")
    void alerts_asInventario() throws Exception {
        when(getLowStockAlertsUseCase.execute(anyInt(), anyInt())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/alerts")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/inventory/alerts VENDEDOR 403")
    void alerts_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/alerts")).andExpect(status().isForbidden());
    }

    @Test @DisplayName("GET /api/v1/inventory/alerts UNAUTH 401")
    void alerts_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/alerts")).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/inventory/alerts/depleted INVENTARIO 200")
    void depleted_asInventario() throws Exception {
        when(getDepletedAlertsUseCase.execute(anyInt(), anyInt())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/alerts/depleted")).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/inventory/alerts/depleted VENDEDOR 403")
    void depleted_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/alerts/depleted")).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("GET /api/v1/inventory/products/{id} ADMIN 200")
    void getInventory_asAdmin() throws Exception {
        when(getInventoryUseCase.getByProductId(any())).thenReturn(InventoryResponse.builder().id(PID).currentStock(50).build());
        mockMvc.perform(get("/api/v1/inventory/products/{productId}", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/inventory/products/{id} VENDEDOR 200")
    void getInventory_asVendedor() throws Exception {
        when(getInventoryUseCase.getByProductId(any())).thenReturn(InventoryResponse.builder().id(PID).currentStock(50).build());
        mockMvc.perform(get("/api/v1/inventory/products/{productId}", PID)).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/v1/inventory/products/{id} UNAUTH 401")
    void getInventory_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/products/{productId}", PID)).andExpect(status().isUnauthorized());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("GET /api/v1/inventory/products/{id}/movements INVENTARIO 200")
    void movements_asInventario() throws Exception {
        when(listMovementsUseCase.listByProductId(any())).thenReturn(new PageResult<>(List.of(), 0L));
        mockMvc.perform(get("/api/v1/inventory/products/{productId}/movements", PID)).andExpect(status().isOk());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("GET /api/v1/inventory/products/{id}/movements VENDEDOR 403")
    void movements_asVendedor() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/products/{productId}/movements", PID)).andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/inventory/products/{id}/entry ADMIN 201")
    void entry_asAdmin() throws Exception {
        when(registerEntryUseCase.registerEntry(any(), anyInt(), any(), any(), any(), any()))
            .thenReturn(MovementResponse.builder().id(MID).quantity(5).movementType("ENTRADA").build());
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/entry", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":5}"))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/inventory/products/{id}/entry VENDEDOR 403")
    void entry_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/entry", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/inventory/products/{id}/exit INVENTARIO 201")
    void exit_asInventario() throws Exception {
        when(registerExitUseCase.registerExit(any(), anyInt(), any(), any(), any(), any()))
            .thenReturn(MovementResponse.builder().id(MID).quantity(3).movementType("SALIDA").build());
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/exit", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/inventory/products/{id}/exit VENDEDOR 403")
    void exit_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/exit", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/inventory/products/{id}/return ADMIN 201")
    void return_asAdmin() throws Exception {
        when(registerReturnUseCase.registerReturn(any(), anyInt(), any(), any(), any(), any()))
            .thenReturn(MovementResponse.builder().id(MID).movementType("DEVOLUCION").build());
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/return", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/inventory/products/{id}/return VENDEDOR 403")
    void return_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/return", PID)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/inventory/products/{id}/adjust INVENTARIO 201")
    void adjust_asInventario() throws Exception {
        when(registerAdjustmentUseCase.registerAdjustment(any(), any(), any()))
            .thenReturn(MovementResponse.builder().id(MID).movementType("AJUSTE").build());
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/adjust", PID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AdjustmentRequest.builder().adjustmentType(com.axiserp.inventory.application.dto.request.AdjustmentRequest.AdjustmentType.POSITIVO).quantity(10).justification("Test ajuste").build())))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "VENDEDOR")
    @DisplayName("POST /api/v1/inventory/products/{id}/adjust VENDEDOR 403")
    void adjust_asVendedor() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/products/{productId}/adjust", PID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AdjustmentRequest.builder().adjustmentType(com.axiserp.inventory.application.dto.request.AdjustmentRequest.AdjustmentType.POSITIVO).quantity(1).justification("Test").build())))
            .andExpect(status().isForbidden());
    }

    @Test @WithCustomUser(role = "ADMIN")
    @DisplayName("POST /api/v1/inventory/movements/{id}/reverse ADMIN 201")
    void reverse_asAdmin() throws Exception {
        when(reverseMovementUseCase.reverse(any(), any(), any()))
            .thenReturn(MovementResponse.builder().id(MID).movementType("REVERSADO").build());
        mockMvc.perform(post("/api/v1/inventory/movements/{movementId}/reverse", MID)
                .param("justification", "error"))
            .andExpect(status().isCreated());
    }

    @Test @WithCustomUser(role = "INVENTARIO")
    @DisplayName("POST /api/v1/inventory/movements/{id}/reverse INVENTARIO 403")
    void reverse_asInventario() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/movements/{movementId}/reverse", MID)
                .param("justification", "error"))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /api/v1/inventory/movements/{id}/reverse UNAUTH 401")
    void reverse_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/movements/{movementId}/reverse", MID)
                .param("justification", "error"))
            .andExpect(status().isUnauthorized());
    }
}
