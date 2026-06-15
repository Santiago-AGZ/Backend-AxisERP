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
 * END-TO-END: Complete User Management Flow through the API Gateway.
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
 *   mvn test -Dtest=UserManagementFlowE2ETest -DfailIfNoTests=false
 * <p>
 * The test performs:
 * 1. Login as ADMIN
 * 2. Create new user (VENDEDOR role)
 * 3. Verify user is PENDIENTE
 * 4. Activate user
 * 5. Verify status changed to ACTIVO
 * 6. Deactivate user
 * 7. Verify deactivated user cannot login
 * 8. Reactivate user
 * 9. Verify reactivated user can login
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Requires all services running on localhost:8080 via docker compose")
class UserManagementFlowE2ETest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final WebClient client = WebClient.create(GATEWAY_URL);

    private static String adminToken;
    private static String adminPassword = "Admin123!";
    private static UUID newUserId;
    private static final String TEST_USER_EMAIL = "e2e.vendedor@axiserp.com";
    private static final String TEST_USER_NAME = "E2E Vendedor";
    private static final String TEST_USER_ROLE = "VENDEDOR";

    @Test
    @Order(1)
    void loginAsAdmin() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "email", "admin@axiserp.com",
                "password", adminPassword
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
    void createUser() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "name", TEST_USER_NAME,
                "email", TEST_USER_EMAIL,
                "role", TEST_USER_ROLE
        ));

        String response = client.post()
                .uri("/api/v1/usuarios")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        newUserId = UUID.fromString(root.get("data").get("id").asText());
        Assertions.assertEquals(TEST_USER_NAME, root.get("data").get("name").asText());
        Assertions.assertEquals(TEST_USER_EMAIL, root.get("data").get("email").asText());
        System.out.println("USER created: id=" + newUserId + " email=" + TEST_USER_EMAIL);
    }

    @Test
    @Order(3)
    void verifyUserIsPendiente() throws Exception {
        String response = client.get()
                .uri("/api/v1/usuarios/{id}", newUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("PENDIENTE", status);
        System.out.println("USER status is PENDIENTE: id=" + newUserId);
    }

    @Test
    @Order(4)
    void activateUser() throws Exception {
        String response = client.patch()
                .uri("/api/v1/usuarios/{id}/activar", newUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        System.out.println("USER activated: id=" + newUserId);
    }

    @Test
    @Order(5)
    void verifyUserIsActivo() throws Exception {
        String response = client.get()
                .uri("/api/v1/usuarios/{id}", newUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("ACTIVO", status);
        System.out.println("USER status is ACTIVO: id=" + newUserId);
    }

    @Test
    @Order(6)
    void deactivateUser() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "currentPassword", adminPassword
        ));

        String response = client.patch()
                .uri("/api/v1/usuarios/{id}/desactivar", newUserId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("INACTIVO", status);
        System.out.println("USER deactivated: id=" + newUserId + " status=" + status);
    }

    @Test
    @Order(7)
    void deactivatedUserCannotLogin() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "email", TEST_USER_EMAIL,
                "password", "dummy123"
        ));

        String response = client.post()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .onErrorReturn("{\"success\":false}")
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertFalse(root.get("success").asBoolean(),
                "Deactivated user login should fail");
        System.out.println("DEACTIVATED user login correctly rejected");
    }

    @Test
    @Order(8)
    void reactivateUser() throws Exception {
        String response = client.patch()
                .uri("/api/v1/usuarios/{id}/reactivar", newUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean());
        String status = root.get("data").get("status").asText();
        Assertions.assertEquals("ACTIVO", status);
        System.out.println("USER reactivated: id=" + newUserId + " status=" + status);
    }

    @Test
    @Order(9)
    void reactivatedUserCanLogin() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of(
                "email", TEST_USER_EMAIL,
                "password", "dummy123"
        ));

        String response = client.post()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchangeToMono(r -> r.bodyToMono(String.class))
                .onErrorReturn("{\"success\":false}")
                .block();

        JsonNode root = MAPPER.readTree(response);
        Assertions.assertTrue(root.get("success").asBoolean(),
                "Reactivated user should be able to login");
        System.out.println("REACTIVATED user login successful");
    }
}
