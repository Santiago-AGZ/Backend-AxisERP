package com.axiserp.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * END-TO-END: Report Flow through the API Gateway.
 * <p>
 * Requires ALL services running (docker compose up).
 * <p>
 * Prerequisites:
 * - An ADMIN user exists in the auth database:
 *   email: admin@axiserp.com
 *   password: Admin123!
 * <p>
 * To run:
 *   cd api-gateway
 *   mvn test -Dtest=ReportFlowE2ETest -DfailIfNoTests=false
 * <p>
 * The test performs:
 * 1. Login as ADMIN
 * 2. Create test data (products, inventory, customer, sale)
 * 3. Get sales report
 * 4. Get inventory report
 * 5. Get dashboard
 * 6. Get top products
 * 7. Export sales to PDF
 * 8. Verify export audit log
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires all services running on localhost:8080 via docker compose")
class ReportFlowE2ETest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final WebClient client = WebClient.create(GATEWAY_URL);

    private static String adminToken;
    private static UUID productId;
    private static UUID customerId;
    private static UUID saleId;

    @Test
    @Order(1)
    void loginAsAdmin() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "email", "admin@axiserp.com",
                "password", "Admin123!"
        ));

        String response = client.post()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        adminToken = root.get("data").get("accessToken").asText();
        Assertions.assertNotNull(adminToken);
        System.out.println("LOGIN: admin@axiserp.com -> token obtained");
    }

    @Test
    @Order(2)
    void createTestData() throws Exception {
        String catBody = MAPPER.writeValueAsString(Map.of(
                "name", "Report E2E Category",
                "description", "Created by ReportFlowE2ETest"
        ));
        String catResp = client.post()
                .uri("/api/v1/categorias")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(catBody)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();
        UUID categoryId = UUID.fromString(MAPPER.readTree(catResp).get("data").get("id").asText());

        String prodBody = MAPPER.writeValueAsString(Map.of(
                "name", "Report E2E Product",
                "codigo", "E2E-RPT-001",
                "description", "Created by ReportFlowE2ETest",
                "categoryId", categoryId.toString(),
                "purchasePrice", 40.00,
                "salePrice", 80.00
        ));
        String prodResp = client.post()
                .uri("/api/v1/productos")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(prodBody)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();
        productId = UUID.fromString(MAPPER.readTree(prodResp).get("data").get("id").asText());

        String invBody = MAPPER.writeValueAsString(Map.of(
                "productId", productId.toString(),
                "initialStock", 200,
                "minStock", 10,
                "maxStock", 500
        ));
        client.post()
                .uri("/api/v1/inventory/initialize")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(invBody)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        String custBody = MAPPER.writeValueAsString(Map.of(
                "codigo", "E2E-RPT-CUS",
                "name", "Report E2E Customer",
                "documentType", "NIT",
                "documentNumber", "900111222-3",
                "email", "e2e.rpt.customer@test.com",
                "phone", "3011112222"
        ));
        String custResp = client.post()
                .uri("/api/v1/customers")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(custBody)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();
        customerId = UUID.fromString(MAPPER.readTree(custResp).get("data").get("id").asText());

        Map<String, Object> item = Map.of(
                "productId", productId.toString(),
                "productName", "Report E2E Product",
                "quantity", 3,
                "unitPrice", 80.00
        );
        String saleBody = MAPPER.writeValueAsString(Map.of(
                "customerId", customerId.toString(),
                "items", List.of(item),
                "notes", "Report E2E sale",
                "discount", 0
        ));
        String saleResp = client.post()
                .uri("/api/v1/sales")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(saleBody)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();
        saleId = UUID.fromString(MAPPER.readTree(saleResp).get("data").get("id").asText());

        client.patch()
                .uri("/api/v1/sales/{id}/confirm", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        client.patch()
                .uri("/api/v1/sales/{id}/pay", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        System.out.println("TEST DATA created: productId=" + productId
                + " customerId=" + customerId + " saleId=" + saleId);
    }

    @Test
    @Order(3)
    void getSalesReport() throws Exception {
        String response = client.get()
                .uri("/api/v1/reports/sales")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        JsonNode data = root.get("data");
        Assertions.assertTrue(data.get("totalTransactions").asLong() > 0);
        System.out.println("SALES REPORT: " + data.get("totalTransactions").asLong()
                + " transactions, revenue=" + data.get("totalRevenue"));
    }

    @Test
    @Order(4)
    void getInventoryReport() throws Exception {
        String response = client.get()
                .uri("/api/v1/reports/inventory")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("INVENTORY REPORT: retrieved successfully");
    }

    @Test
    @Order(5)
    void getDashboard() throws Exception {
        String response = client.get()
                .uri("/api/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("DASHBOARD: retrieved successfully");
    }

    @Test
    @Order(6)
    void getTopProducts() throws Exception {
        String response = client.get()
                .uri("/api/v1/reports/top-products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("TOP PRODUCTS: retrieved successfully");
    }

    @Test
    @Order(7)
    void exportSalesToPdf() {
        byte[] pdfBytes = client.get()
                .uri("/api/v1/reports/sales/export/pdf")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(byte[].class))
                .block();

        Assertions.assertNotNull(pdfBytes);
        Assertions.assertTrue(pdfBytes.length > 0);
        // PDF magic bytes: %PDF
        Assertions.assertTrue(pdfBytes.length >= 4
                && pdfBytes[0] == 0x25 && pdfBytes[1] == 0x50
                && pdfBytes[2] == 0x44 && pdfBytes[3] == 0x46);
        System.out.println("PDF EXPORT: " + pdfBytes.length + " bytes");
    }

    @Test
    @Order(8)
    void verifyExportAuditLog() throws Exception {
        String response = client.get()
                .uri("/api/v1/reports/audit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("EXPORT AUDIT LOG: retrieved successfully");
    }
}
