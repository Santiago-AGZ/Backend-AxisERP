package com.axiserp.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * END-TO-END: Complete Sale Flow through the API Gateway.
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
 *   mvn test -Dtest=FullSaleFlowE2ETest -DfailIfNoTests=false
 * <p>
 * The test performs:
 * 1. Login as ADMIN
 * 2. Create a category
 * 3. Create a product
 * 4. Initialize inventory
 * 5. Create a customer
 * 6. Create a sale
 * 7. Confirm the sale
 * 8. Pay the sale
 * 9. Get invoice
 * 10. Verify audit log
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires all services running on localhost:8080 via docker compose")
class FullSaleFlowE2ETest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final WebClient client = WebClient.create(GATEWAY_URL);

    private static String adminToken;
    private static String adminRefreshToken;
    private static UUID categoryId;
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
        JsonNode data = root.get("data");
        adminToken = data.get("accessToken").asText();
        adminRefreshToken = data.get("refreshToken").asText();

        Assertions.assertNotNull(adminToken);
        Assertions.assertEquals("ADMIN", data.get("role").asText());
        System.out.println("LOGIN: admin@axiserp.com -> accessToken="
                + adminToken.substring(0, 20) + "...");
    }

    @Test
    @Order(2)
    void createCategory() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", "E2E Test Category",
                "description", "Created by FullSaleFlowE2ETest"
        ));

        String response = client.post()
                .uri("/api/v1/categorias")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        categoryId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertNotNull(categoryId);
        System.out.println("CATEGORY created: id=" + categoryId);
    }

    @Test
    @Order(3)
    void createProduct() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", "E2E Test Product",
                "codigo", "E2E-PROD-001",
                "description", "Created by FullSaleFlowE2ETest",
                "categoryId", categoryId.toString(),
                "purchasePrice", 50.00,
                "salePrice", 100.00
        ));

        String response = client.post()
                .uri("/api/v1/productos")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        productId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertNotNull(productId);
        System.out.println("PRODUCT created: id=" + productId);
    }

    @Test
    @Order(4)
    void initializeInventory() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "productId", productId.toString(),
                "initialStock", 100,
                "minStock", 10,
                "maxStock", 500
        ));

        String response = client.post()
                .uri("/api/v1/inventory/initialize")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        JsonNode data = root.get("data");
        Assertions.assertEquals(100, data.get("currentStock").asInt());
        System.out.println("INVENTORY initialized: productId=" + productId
                + " stock=" + data.get("currentStock").asInt());
    }

    @Test
    @Order(5)
    void createCustomer() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "codigo", "E2E-CUS-001",
                "name", "E2E Test Customer",
                "documentType", "NIT",
                "documentNumber", "900123456-7",
                "email", "e2e.customer@test.com",
                "phone", "3001234567",
                "address", "Calle E2E #123"
        ));

        String response = client.post()
                .uri("/api/v1/customers")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        customerId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertNotNull(customerId);
        System.out.println("CUSTOMER created: id=" + customerId);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(6)
    void createSale() throws Exception {
        Map<String, Object> item = Map.of(
                "productId", productId.toString(),
                "productName", "E2E Test Product",
                "quantity", 2,
                "unitPrice", 100.00
        );
        String body = MAPPER.writeValueAsString(Map.of(
                "customerId", customerId.toString(),
                "items", List.of(item),
                "notes", "E2E sale created by FullSaleFlowE2ETest",
                "discount", 0
        ));

        String response = client.post()
                .uri("/api/v1/sales")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        saleId = UUID.fromString(root.get("data").get("id").asText());
        String status = root.get("data").get("status").asText();
        Assertions.assertNotNull(saleId);
        System.out.println("SALE created: id=" + saleId + " status=" + status);
    }

    @Test
    @Order(7)
    void confirmSale() throws Exception {
        String response = client.patch()
                .uri("/api/v1/sales/{id}/confirm", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("CONFIRMADA", status);
        System.out.println("SALE confirmed: id=" + saleId + " status=" + status);
    }

    @Test
    @Order(8)
    void paySale() throws Exception {
        String response = client.patch()
                .uri("/api/v1/sales/{id}/pay", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("PAGADA", status);
        System.out.println("SALE paid: id=" + saleId + " status=" + status);
    }

    @Test
    @Order(9)
    void getInvoice() throws Exception {
        String response = client.get()
                .uri("/api/v1/invoices/by-sale/{saleId}", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        JsonNode data = root.get("data");
        UUID invoiceId = UUID.fromString(data.get("id").asText());
        Assertions.assertNotNull(invoiceId);
        BigDecimal total = new BigDecimal(data.get("total").asText());
        Assertions.assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
        System.out.println("INVOICE generated: id=" + invoiceId + " total=" + total);
    }

    @Test
    @Order(10)
    void verifyAuditLog() throws Exception {
        String response = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/audit-log")
                        .queryParam("action", "VENTA_CREADA")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        JsonNode data = root.get("data");
        Assertions.assertTrue(data.isArray() && data.size() > 0);
        System.out.println("AUDIT log verified: " + data.size() + " entries found");
    }
}
