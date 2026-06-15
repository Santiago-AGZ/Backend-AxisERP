package com.axiserp.gateway.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

/**
 * END-TO-END: Complete Inventory Flow through the API Gateway.
 * <p>
 * Requires ALL services running (docker compose up).
 * <p>
 * Prerequisites:
 * - An INVENTARIO user exists in the auth database:
 *   email: inventario@axiserp.com
 *   password: Inventario123!
 * <p>
 * To run:
 *   cd api-gateway
 *   mvn test -Dtest=FullInventoryFlowE2ETest -DfailIfNoTests=false
 * <p>
 * The test performs:
 * 1. Login as INVENTARIO
 * 2. Create category
 * 3. Create product
 * 4. Initialize inventory
 * 5. Register entry
 * 6. Register exit
 * 7. Check alerts
 * 8. Reverse movement
 * 9. Verify audit trail
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires all services running on localhost:8080 via docker compose")
class FullInventoryFlowE2ETest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final WebClient client = WebClient.create(GATEWAY_URL);

    private static String inventarioToken;
    private static UUID categoryId;
    private static UUID productId;
    private static UUID entryMovementId;
    private static UUID exitMovementId;

    @Test
    @Order(1)
    void loginAsInventario() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "email", "inventario@axiserp.com",
                "password", "Inventario123!"
        ));

        String response = client.post()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        inventarioToken = root.get("data").get("accessToken").asText();
        Assertions.assertNotNull(inventarioToken);
        Assertions.assertEquals("INVENTARIO", root.get("data").get("role").asText());
        System.out.println("LOGIN: inventario@axiserp.com -> token obtained");
    }

    @Test
    @Order(2)
    void createCategory() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", "Inventory E2E Category",
                "description", "Created by FullInventoryFlowE2ETest"
        ));

        String response = client.post()
                .uri("/api/v1/categorias")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
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
                "name", "Inventory E2E Product",
                "codigo", "E2E-INV-001",
                "description", "Created by FullInventoryFlowE2ETest",
                "categoryId", categoryId.toString(),
                "purchasePrice", 25.00,
                "salePrice", 60.00
        ));

        String response = client.post()
                .uri("/api/v1/productos")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
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
                "initialStock", 50,
                "minStock", 5,
                "maxStock", 200,
                "notes", "Initial stock for E2E test"
        ));

        String response = client.post()
                .uri("/api/v1/inventory/initialize")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        JsonNode data = root.get("data");
        Assertions.assertEquals(50, data.get("currentStock").asInt());
        System.out.println("INVENTORY initialized: stock="
                + data.get("currentStock").asInt());
    }

    @Test
    @Order(5)
    void registerEntry() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "quantity", 20,
                "referenceType", "PURCHASE",
                "referenceId", UUID.randomUUID().toString(),
                "notes", "E2E entry movement"
        ));

        String response = client.post()
                .uri("/api/v1/inventory/products/{productId}/entry", productId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        entryMovementId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertEquals("ENTRADA", root.get("data").get("movementType").asText());
        System.out.println("ENTRY registered: movementId=" + entryMovementId);
    }

    @Test
    @Order(6)
    void registerExit() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "quantity", 10,
                "referenceType", "SALE",
                "referenceId", UUID.randomUUID().toString(),
                "notes", "E2E exit movement"
        ));

        String response = client.post()
                .uri("/api/v1/inventory/products/{productId}/exit", productId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        exitMovementId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertEquals("SALIDA", root.get("data").get("movementType").asText());
        System.out.println("EXIT registered: movementId=" + exitMovementId);
    }

    @Test
    @Order(7)
    void checkAlerts() throws Exception {
        String response = client.get()
                .uri("/api/v1/inventory/alerts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("ALERTS checked: returned data present");
    }

    @Test
    @Order(8)
    void reverseMovement() throws Exception {
        String response = client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/inventory/movements/{movementId}/reverse")
                        .queryParam("justification", "E2E test reversal")
                        .build(exitMovementId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("MOVEMENT reversed: movementId=" + exitMovementId);
    }

    @Test
    @Order(9)
    void verifyInventoryAuditTrail() throws Exception {
        String response = client.get()
                .uri("/api/v1/inventory/products/{productId}/movements", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + inventarioToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("AUDIT trail: movements retrieved for productId=" + productId);
    }
}
