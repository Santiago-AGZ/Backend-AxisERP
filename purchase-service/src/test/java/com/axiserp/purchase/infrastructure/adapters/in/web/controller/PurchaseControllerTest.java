package com.axiserp.purchase.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.axiserp.purchase.application.dto.request.CreatePurchaseRequest;
import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.request.PurchaseItemRequest;
import com.axiserp.purchase.application.dto.request.ReceiveItemRequest;
import com.axiserp.purchase.application.dto.request.ReceivePurchaseRequest;
import com.axiserp.purchase.application.dto.request.UpdateSupplierRequest;
import com.axiserp.purchase.application.dto.response.PurchaseResponse;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.model.PurchaseStatus;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse.PaginationMeta;
import com.axiserp.purchase.ports.input.CancelPurchaseUseCase;
import com.axiserp.purchase.ports.input.CreatePurchaseUseCase;
import com.axiserp.purchase.ports.input.CreateSupplierUseCase;
import com.axiserp.purchase.ports.input.DeactivateSupplierUseCase;
import com.axiserp.purchase.ports.input.GetPurchaseUseCase;
import com.axiserp.purchase.ports.input.GetSupplierUseCase;
import com.axiserp.purchase.ports.input.ListPurchasesUseCase;
import com.axiserp.purchase.ports.input.ListSuppliersUseCase;
import com.axiserp.purchase.ports.input.ReactivateSupplierUseCase;
import com.axiserp.purchase.ports.input.ReceivePurchaseUseCase;
import com.axiserp.purchase.ports.input.UpdatePurchaseStatusUseCase;
import com.axiserp.purchase.ports.input.UpdateSupplierUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierController")
class SupplierControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateSupplierUseCase createSupplierUseCase;
    @Mock
    private GetSupplierUseCase getSupplierUseCase;
    @Mock
    private ListSuppliersUseCase listSuppliersUseCase;
    @Mock
    private DeactivateSupplierUseCase deactivateSupplierUseCase;
    @Mock
    private ReactivateSupplierUseCase reactivateSupplierUseCase;
    @Mock
    private UpdateSupplierUseCase updateSupplierUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        var authentication = new UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000001", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new SupplierController(
                        createSupplierUseCase, getSupplierUseCase, listSuppliersUseCase,
                        deactivateSupplierUseCase, reactivateSupplierUseCase, updateSupplierUseCase))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/suppliers - should return 201")
    void createSupplier_success() throws Exception {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .codigo("PROV-000001")
                .name("Test")
                .nit("123456789")
                .build();

        SupplierResponse response = SupplierResponse.builder()
                .id(UUID.randomUUID())
                .codigo("PROV-000001")
                .name("Test")
                .nit("123456789")
                .status(SupplierStatus.ACTIVO)
                .build();

        when(createSupplierUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("PROV-000001"));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/{id} - should return 200")
    void getSupplier_success() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = SupplierResponse.builder()
                .id(id)
                .codigo("PROV-000001")
                .name("Test")
                .build();

        when(getSupplierUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/suppliers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers - should return 200")
    void listSuppliers_success() throws Exception {
        when(listSuppliersUseCase.execute()).thenReturn(List.of());
        when(listSuppliersUseCase.countAll()).thenReturn(0L);

        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/suppliers?search=test - should search")
    void listSuppliers_withSearch() throws Exception {
        when(listSuppliersUseCase.execute(anyString(), anyInt(), anyInt())).thenReturn(List.of());
        when(listSuppliersUseCase.countBySearch(anyString())).thenReturn(0L);

        mockMvc.perform(get("/api/v1/suppliers")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/{id} - should return 200")
    void updateSupplier_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                .name("Updated")
                .build();

        when(updateSupplierUseCase.execute(eq(id), any())).thenReturn(
                SupplierResponse.builder().id(id).name("Updated").build());

        mockMvc.perform(put("/api/v1/suppliers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/suppliers/{id}/deactivate - should return 200")
    void deactivateSupplier_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(deactivateSupplierUseCase.execute(id)).thenReturn(
                SupplierResponse.builder().id(id).status(SupplierStatus.ELIMINADO).build());

        mockMvc.perform(patch("/api/v1/suppliers/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/suppliers/{id}/reactivate - should return 200")
    void reactivateSupplier_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(reactivateSupplierUseCase.execute(id)).thenReturn(
                SupplierResponse.builder().id(id).status(SupplierStatus.ACTIVO).build());

        mockMvc.perform(patch("/api/v1/suppliers/{id}/reactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseController")
class PurchaseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreatePurchaseUseCase createPurchaseUseCase;
    @Mock
    private GetPurchaseUseCase getPurchaseUseCase;
    @Mock
    private ListPurchasesUseCase listPurchasesUseCase;
    @Mock
    private UpdatePurchaseStatusUseCase updatePurchaseStatusUseCase;
    @Mock
    private ReceivePurchaseUseCase receivePurchaseUseCase;
    @Mock
    private CancelPurchaseUseCase cancelPurchaseUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID purchaseId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        var authentication = new UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000001", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PurchaseController(
                        createPurchaseUseCase, getPurchaseUseCase, listPurchasesUseCase,
                        updatePurchaseStatusUseCase, receivePurchaseUseCase, cancelPurchaseUseCase))
                .build();

        purchaseId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /api/v1/purchases - should return 201")
    void createPurchase_success() throws Exception {
        CreatePurchaseRequest request = CreatePurchaseRequest.builder()
                .supplierId(UUID.randomUUID())
                .items(List.of(PurchaseItemRequest.builder()
                        .productId(UUID.randomUUID())
                        .productName("Test")
                        .quantity(1)
                        .unitPrice(new BigDecimal("10.00"))
                        .build()))
                .build();

        when(createPurchaseUseCase.execute(any(), any())).thenReturn(
                PurchaseResponse.builder().id(purchaseId).status(PurchaseStatus.BORRADOR).build());

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(purchaseId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/purchases/{id} - should return 200")
    void getPurchase_success() throws Exception {
        when(getPurchaseUseCase.execute(purchaseId)).thenReturn(
                PurchaseResponse.builder().id(purchaseId).status(PurchaseStatus.BORRADOR).build());

        mockMvc.perform(get("/api/v1/purchases/{id}", purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/purchases - should return 200")
    void listPurchases_success() throws Exception {
        when(listPurchasesUseCase.execute()).thenReturn(List.of());
        when(listPurchasesUseCase.countAll()).thenReturn(0L);

        mockMvc.perform(get("/api/v1/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/purchases/{id}/status?status=PENDIENTE - should return 200")
    void updateStatus_success() throws Exception {
        when(updatePurchaseStatusUseCase.execute(purchaseId, PurchaseStatus.PENDIENTE)).thenReturn(
                PurchaseResponse.builder().id(purchaseId).status(PurchaseStatus.PENDIENTE).build());

        mockMvc.perform(patch("/api/v1/purchases/{id}/status", purchaseId)
                        .param("status", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/purchases/{id}/receive - should return 200")
    void receive_success() throws Exception {
        ReceivePurchaseRequest request = ReceivePurchaseRequest.builder()
                .items(List.of(ReceiveItemRequest.builder()
                        .itemId(UUID.randomUUID())
                        .receivedQuantity(5)
                        .build()))
                .build();

        when(receivePurchaseUseCase.execute(eq(purchaseId), any())).thenReturn(
                PurchaseResponse.builder().id(purchaseId).status(PurchaseStatus.RECIBIDA).build());

        mockMvc.perform(post("/api/v1/purchases/{id}/receive", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/purchases/{id}/cancel - should return 200")
    void cancel_success() throws Exception {
        when(cancelPurchaseUseCase.execute(purchaseId)).thenReturn(
                PurchaseResponse.builder().id(purchaseId).status(PurchaseStatus.CANCELADA).build());

        mockMvc.perform(patch("/api/v1/purchases/{id}/cancel", purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
