package com.axiserp.inventory.infrastructure.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.application.shared.PageResult;
import com.axiserp.inventory.ports.input.GetDepletedAlertsUseCase;
import com.axiserp.inventory.ports.input.GetInventoryUseCase;
import com.axiserp.inventory.ports.input.GetLowStockAlertsUseCase;
import com.axiserp.inventory.ports.input.InitializeInventoryUseCase;
import com.axiserp.inventory.ports.input.ListMovementsUseCase;
import com.axiserp.inventory.ports.input.ListProductsUseCase;
import com.axiserp.inventory.ports.input.RegisterAdjustmentUseCase;
import com.axiserp.inventory.ports.input.RegisterEntryUseCase;
import com.axiserp.inventory.ports.input.RegisterExitUseCase;
import com.axiserp.inventory.ports.input.RegisterReturnUseCase;
import com.axiserp.inventory.ports.input.ReverseMovementUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryController")
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InitializeInventoryUseCase initializeInventoryUseCase;
    @Mock
    private ListProductsUseCase listProductsUseCase;
    @Mock
    private GetLowStockAlertsUseCase getLowStockAlertsUseCase;
    @Mock
    private GetDepletedAlertsUseCase getDepletedAlertsUseCase;
    @Mock
    private GetInventoryUseCase getInventoryUseCase;
    @Mock
    private ListMovementsUseCase listMovementsUseCase;
    @Mock
    private RegisterEntryUseCase registerEntryUseCase;
    @Mock
    private RegisterExitUseCase registerExitUseCase;
    @Mock
    private RegisterReturnUseCase registerReturnUseCase;
    @Mock
    private RegisterAdjustmentUseCase registerAdjustmentUseCase;
    @Mock
    private ReverseMovementUseCase reverseMovementUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        var authentication = new UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000001", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new InventoryController(
                        initializeInventoryUseCase, listProductsUseCase,
                        getLowStockAlertsUseCase, getDepletedAlertsUseCase,
                        getInventoryUseCase, listMovementsUseCase,
                        registerEntryUseCase, registerExitUseCase,
                        registerReturnUseCase, registerAdjustmentUseCase,
                        reverseMovementUseCase))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/inventory/initialize - should return 201")
    void initialize_success() throws Exception {
        UUID productId = UUID.randomUUID();
        InitializeInventoryRequest request = InitializeInventoryRequest.builder()
                .productId(productId)
                .initialStock(100)
                .minStock(10)
                .maxStock(200)
                .build();
        InventoryResponse response = InventoryResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(100)
                .minStock(10)
                .maxStock(200)
                .build();

        when(initializeInventoryUseCase.initialize(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventory/initialize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.currentStock").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/products/{productId} - should return 200")
    void getInventory_success() throws Exception {
        UUID productId = UUID.randomUUID();
        InventoryResponse response = InventoryResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currentStock(50)
                .minStock(5)
                .maxStock(100)
                .build();

        when(getInventoryUseCase.getByProductId(productId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.currentStock").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/alerts - should return 200 with pagination")
    void getLowStockAlerts_success() throws Exception {
        when(getLowStockAlertsUseCase.execute(0, 10)).thenReturn(
                new PageResult<>(List.of(), 0L));

        mockMvc.perform(get("/api/v1/inventory/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/products/{id}/entry - should return 201")
    void registerEntry_success() throws Exception {
        UUID productId = UUID.randomUUID();

        when(registerEntryUseCase.registerEntry(eq(productId), eq(10), any(), any(), any(), any()))
                .thenReturn(MovementResponse.builder()
                        .id(UUID.randomUUID())
                        .quantity(10)
                        .movementType("ENTRADA")
                        .build());

        mockMvc.perform(post("/api/v1/inventory/products/{productId}/entry", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
