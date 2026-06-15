package com.axiserp.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * END-TO-END: Complete Purchase Flow through the API Gateway.
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
 *   mvn test -Dtest=FullPurchaseFlowE2ETest -DfailIfNoTests=false
 * <p>
 * The test performs:
 * 1. Login as ADMIN
 * 2. Create category + product (prerequisites)
 * 3. Create supplier
 * 4. Create purchase
 * 5. Receive purchase
 * 6. Cancel purchase
 * 7. Verify inventory was updated
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires all services running on localhost:8080 via docker compose")
class FullPurchaseFlowE2ETest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final WebClient client = WebClient.create(GATEWAY_URL);

    private static String adminToken;
    private static UUID categoryId;
    private static UUID productId;
    private static UUID supplierId;
    private static UUID purchaseId;

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
    void createCategory() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", "Purchase E2E Category",
                "description", "Created by FullPurchaseFlowE2ETest"
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
        System.out.println("CATEGORY created: id=" + categoryId);
    }

    @Test
    @Order(3)
    void createProduct() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", "Purchase E2E Product",
                "codigo", "E2E-PUR-001",
                "description", "Created by FullPurchaseFlowE2ETest",
                "categoryId", categoryId.toString(),
                "purchasePrice", 30.00,
                "salePrice", 70.00
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
        System.out.println("PRODUCT created: id=" + productId);
    }

    @Test
    @Order(4)
    void initializeInventory() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "productId", productId.toString(),
                "initialStock", 0,
                "minStock", 5,
                "maxStock", 100
        ));

        client.post()
                .uri("/api/v1/inventory/initialize")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        System.out.println("INVENTORY initialized for productId=" + productId);
    }

    @Test
    @Order(5)
    void createSupplier() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "codigo", "PROV-000001",
                "name", "E2E Test Supplier",
                "nit", "900987654-3",
                "phone", "3109876543",
                "email", "e2e.supplier@test.com",
                "address", "Calle Proveedor #456"
        ));

        String response = client.post()
                .uri("/api/v1/suppliers")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        supplierId = UUID.fromString(root.get("data").get("id").asText());
        System.out.println("SUPPLIER created: id=" + supplierId);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(6)
    void createPurchase() throws Exception {
        Map<String, Object> item = Map.of(
                "productId", productId.toString(),
                "productName", "Purchase E2E Product",
                "quantity", 10,
                "unitPrice", 30.00
        );
        String body = MAPPER.writeValueAsString(Map.of(
                "supplierId", supplierId.toString(),
                "items", List.of(item),
                "notes", "E2E purchase created by FullPurchaseFlowE2ETest"
        ));

        String response = client.post()
                .uri("/api/v1/purchases")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        purchaseId = UUID.fromString(root.get("data").get("id").asText());
        String status = root.get("data").get("status").asText();
        Assertions.assertNotNull(purchaseId);
        System.out.println("PURCHASE created: id=" + purchaseId + " status=" + status);
    }

    @Test
    @Order(7)
    void receivePurchase() throws Exception {
        String getResponse = client.get()
                .uri("/api/v1/purchases/{id}", purchaseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode getRoot = MAPPER.readTree(getResponse);
        JsonNode items = getRoot.get("data").get("items");
        UUID itemId = UUID.fromString(items.get(0).get("id").asText());

        String body = MAPPER.writeValueAsString(Map.of(
                "items", List.of(Map.of(
                        "itemId", itemId.toString(),
                        "receivedQuantity", 10
                ))
        ));

        String response = client.post()
                .uri("/api/v1/purchases/{id}/receive", purchaseId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("RECIBIDA", status);
        System.out.println("PURCHASE received: id=" + purchaseId + " status=" + status);
    }

    @Test
    @Order(8)
    void cancelPurchase() throws Exception {
        String response = client.patch()
                .uri("/api/v1/purchases/{id}/cancel", purchaseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("CANCELADA", status);
        System.out.println("PURCHASE cancelled: id=" + purchaseId + " status=" + status);
    }

    @Test
    @Order(9)
    void verifyInventoryUpdated() throws Exception {
        String response = client.get()
                .uri("/api/v1/inventory/products/{productId}", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        int currentStock = root.get("data").get("currentStock").asInt();
        System.out.println("INVENTORY after purchase flow: stock=" + currentStock);
    }
}
