package com.axiserp.sales.infrastructure.adapters.in.web.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.application.dto.request.UpdateCustomerRequest;
import com.axiserp.sales.application.dto.response.CustomerResponse;
import com.axiserp.sales.application.dto.response.InvoiceResponse;
import com.axiserp.sales.application.dto.response.PaginatedResponse;
import com.axiserp.sales.application.dto.response.SaleResponse;
import com.axiserp.sales.application.service.ExcelExportService;
import com.axiserp.sales.application.service.PdfExportService;
import com.axiserp.sales.domain.model.SaleStatus;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.input.ConfirmSaleUseCase;
import com.axiserp.sales.ports.input.CreateCustomerUseCase;
import com.axiserp.sales.ports.input.CreateSaleUseCase;
import com.axiserp.sales.ports.input.DeactivateCustomerUseCase;
import com.axiserp.sales.ports.input.GetCustomerHistoryUseCase;
import com.axiserp.sales.ports.input.GetCustomerUseCase;
import com.axiserp.sales.ports.input.GetInvoiceUseCase;
import com.axiserp.sales.ports.input.GetSaleUseCase;
import com.axiserp.sales.ports.input.ListCustomersUseCase;
import com.axiserp.sales.ports.input.ListSalesUseCase;
import com.axiserp.sales.ports.input.PaySaleUseCase;
import com.axiserp.sales.ports.input.ReactivateCustomerUseCase;
import com.axiserp.sales.ports.input.UpdateCustomerUseCase;
import com.axiserp.sales.ports.input.VoidSaleUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleController")
class SaleControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateSaleUseCase createSaleUseCase;
    @Mock private GetSaleUseCase getSaleUseCase;
    @Mock private ListSalesUseCase listSalesUseCase;
    @Mock private ConfirmSaleUseCase confirmSaleUseCase;
    @Mock private PaySaleUseCase paySaleUseCase;
    @Mock private VoidSaleUseCase voidSaleUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UUID saleId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SaleController(createSaleUseCase, getSaleUseCase,
                        listSalesUseCase, confirmSaleUseCase, paySaleUseCase, voidSaleUseCase))
                .build();

        saleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/v1/sales/{id} - should return 200")
    void getSale_success() throws Exception {
        when(getSaleUseCase.getById(saleId))
                .thenReturn(SaleResponse.builder().id(saleId).build());

        mockMvc.perform(get("/api/v1/sales/{id}", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/sales - should return 200")
    void listSales_success() throws Exception {
        when(listSalesUseCase.list(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(PaginatedResponse.<SaleResponse>builder()
                        .content(List.of())
                        .totalRecords(0)
                        .build());

        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/sales/{id}/confirm - should return 200")
    void confirmSale_success() throws Exception {
        when(confirmSaleUseCase.confirm(saleId))
                .thenReturn(SaleResponse.builder().id(saleId).status(SaleStatus.CONFIRMADA.name()).build());

        mockMvc.perform(patch("/api/v1/sales/{id}/confirm", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/sales/{id}/pay - should return 200")
    void paySale_success() throws Exception {
        when(paySaleUseCase.pay(saleId))
                .thenReturn(SaleResponse.builder().id(saleId).status(SaleStatus.PAGADA.name()).build());

        mockMvc.perform(patch("/api/v1/sales/{id}/pay", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/sales/{id}/void - should return 200")
    void voidSale_success() throws Exception {
        when(voidSaleUseCase.voidSale(saleId))
                .thenReturn(SaleResponse.builder().id(saleId).status(SaleStatus.ANULADA.name()).build());

        mockMvc.perform(patch("/api/v1/sales/{id}/void", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController")
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateCustomerUseCase createCustomerUseCase;
    @Mock private GetCustomerUseCase getCustomerUseCase;
    @Mock private ListCustomersUseCase listCustomersUseCase;
    @Mock private DeactivateCustomerUseCase deactivateCustomerUseCase;
    @Mock private ReactivateCustomerUseCase reactivateCustomerUseCase;
    @Mock private UpdateCustomerUseCase updateCustomerUseCase;
    @Mock private GetCustomerHistoryUseCase getCustomerHistoryUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerController(
                        createCustomerUseCase, getCustomerUseCase, listCustomersUseCase,
                        deactivateCustomerUseCase, reactivateCustomerUseCase,
                        updateCustomerUseCase, getCustomerHistoryUseCase))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/customers/{codigo} - should return 200")
    void getCustomer_success() throws Exception {
        when(getCustomerUseCase.getByCodigo("CLI-000001"))
                .thenReturn(CustomerResponse.builder().codigo("CLI-000001").build());

        mockMvc.perform(get("/api/v1/customers/{codigo}", "CLI-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/customers - should return 200")
    void listCustomers_success() throws Exception {
        when(listCustomersUseCase.list(any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(PaginatedResponse.<CustomerResponse>builder()
                        .content(List.of())
                        .totalRecords(0)
                        .build());

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/deactivate - should return 200")
    void deactivateCustomer_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(deactivateCustomerUseCase.deactivate(id))
                .thenReturn(CustomerResponse.builder().id(id).status(CustomerStatus.INACTIVO.name()).build());

        mockMvc.perform(patch("/api/v1/customers/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/reactivate - should return 200")
    void reactivateCustomer_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(reactivateCustomerUseCase.reactivate(id))
                .thenReturn(CustomerResponse.builder().id(id).status(CustomerStatus.ACTIVO.name()).build());

        mockMvc.perform(patch("/api/v1/customers/{id}/reactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} - should return 200")
    void updateCustomer_success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setName("Updated");

        when(updateCustomerUseCase.execute(eq(id), any()))
                .thenReturn(CustomerResponse.builder().id(id).name("Updated").build());

        mockMvc.perform(put("/api/v1/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{customerId}/history - should return 200")
    void getCustomerHistory_success() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(getCustomerHistoryUseCase.execute(customerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/{customerId}/history", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceController")
class InvoiceControllerTest {

    private MockMvc mockMvc;

    @Mock private GetInvoiceUseCase getInvoiceUseCase;
    @Mock private PdfExportService pdfExportService;
    @Mock private ExcelExportService excelExportService;

    private UUID invoiceId;
    private UUID saleId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InvoiceController(getInvoiceUseCase, pdfExportService, excelExportService))
                .build();

        invoiceId = UUID.randomUUID();
        saleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/v1/invoices/{id} - should return 200")
    void getInvoice_success() throws Exception {
        when(getInvoiceUseCase.getById(invoiceId))
                .thenReturn(InvoiceResponse.builder().id(invoiceId).invoiceNumber(1L).build());

        mockMvc.perform(get("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.invoiceNumber").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/by-sale/{saleId} - should return 200")
    void getInvoiceBySale_success() throws Exception {
        when(getInvoiceUseCase.getBySaleId(saleId))
                .thenReturn(InvoiceResponse.builder().id(invoiceId).saleId(saleId).build());

        mockMvc.perform(get("/api/v1/invoices/by-sale/{saleId}", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/{saleId}/pdf - should return 200")
    void generateInvoicePdf_success() throws Exception {
        when(pdfExportService.generateInvoicePdf(saleId)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/v1/invoices/{saleId}/pdf", saleId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("GET /api/v1/invoices/{saleId}/excel - should return 200")
    void generateInvoiceExcel_success() throws Exception {
        when(excelExportService.generateInvoiceExcel(saleId)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/v1/invoices/{saleId}/excel", saleId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
