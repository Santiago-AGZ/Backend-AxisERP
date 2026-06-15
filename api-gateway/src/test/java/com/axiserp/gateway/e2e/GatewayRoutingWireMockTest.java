package com.axiserp.gateway.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * CI integration test that verifies API Gateway routing using WireMock
 * to simulate downstream services.
 * <p>
 * Unlike the @Disabled E2E tests, this test runs in CI and validates that
 * the gateway correctly routes requests to the appropriate services.
 * <p>
 * The test uses a single WireMock instance since each service has unique
 * path prefixes that the gateway can route to.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingWireMockTest {

    private static WireMockServer wireMock;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
        System.out.println("WireMock started on port: " + wireMock.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    /**
     * Override all service URLs to point to the single WireMock instance.
     * Since each service has unique path prefixes, the gateway can distinguish
     * them by path alone.
     */
    @DynamicPropertySource
    static void configureServiceUrls(DynamicPropertyRegistry registry) {
        String wireMockUrl = "http://localhost:" + wireMock.port();
        registry.add("auth-service-url", () -> wireMockUrl);
        registry.add("catalog-service-url", () -> wireMockUrl);
        registry.add("inventory-service-url", () -> wireMockUrl);
        registry.add("sales-service-url", () -> wireMockUrl);
        registry.add("purchase-service-url", () -> wireMockUrl);
        registry.add("report-service-url", () -> wireMockUrl);
    }

    @Test
    @DisplayName("Gateway routes auth login to auth-service")
    void authLoginRoute() throws Exception {
        String loginJson = MAPPER.writeValueAsString(Map.of(
                "email", "admin@axiserp.com",
                "password", "Admin123!"
        ));
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "SUCCESS",
                "data", Map.of(
                        "accessToken", "test-jwt-token",
                        "refreshToken", "test-refresh-token",
                        "role", "ADMIN",
                        "name", "Admin"
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/auth/login"))
                .withHeader(HttpHeaders.CONTENT_TYPE, containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.accessToken").isEqualTo("test-jwt-token")
                .jsonPath("$.data.role").isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Gateway routes product creation to catalog-service")
    void catalogCreateProductRoute() throws Exception {
        UUID categoryId = UUID.randomUUID();
        String productJson = MAPPER.writeValueAsString(Map.of(
                "name", "Test Product",
                "codigo", "TST-001",
                "description", "WireMock test product",
                "categoryId", categoryId.toString(),
                "purchasePrice", 50.00,
                "salePrice", 100.00
        ));
        UUID productId = UUID.randomUUID();
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "CREATED",
                "data", Map.of(
                        "id", productId.toString(),
                        "name", "Test Product",
                        "codigo", "TST-001",
                        "status", "ACTIVO"
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/productos"))
                .withHeader(HttpHeaders.AUTHORIZATION, containing("Bearer"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/productos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(productId.toString());
    }

    @Test
    @DisplayName("Gateway routes inventory initialization to inventory-service")
    void inventoryInitializeRoute() throws Exception {
        UUID productId = UUID.randomUUID();
        String invJson = MAPPER.writeValueAsString(Map.of(
                "productId", productId.toString(),
                "initialStock", 100,
                "minStock", 10,
                "maxStock", 500
        ));
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "CREATED",
                "data", Map.of(
                        "id", UUID.randomUUID().toString(),
                        "productId", productId.toString(),
                        "currentStock", 100,
                        "lowStock", false,
                        "depleted", false
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/inventory/initialize"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/inventory/initialize")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.currentStock").isEqualTo(100);
    }

    @Test
    @DisplayName("Gateway routes sale creation to sales-service")
    void salesCreateSaleRoute() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Map<String, Object> item = Map.of(
                "productId", productId.toString(),
                "productName", "Test Product",
                "quantity", 2,
                "unitPrice", 100.00
        );
        String saleJson = MAPPER.writeValueAsString(Map.of(
                "customerId", customerId.toString(),
                "items", java.util.List.of(item),
                "notes", "WireMock test sale",
                "discount", 0
        ));
        UUID saleId = UUID.randomUUID();
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "CREATED",
                "data", Map.of(
                        "id", saleId.toString(),
                        "saleNumber", "V-000001",
                        "status", "PENDIENTE",
                        "total", 200.00
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/sales"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/sales")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(saleJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(saleId.toString())
                .jsonPath("$.data.status").isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Gateway routes sale confirm to sales-service")
    void salesConfirmRoute() {
        UUID saleId = UUID.randomUUID();
        String responseJson = "{\"success\":true,\"code\":\"SUCCESS\",\"data\":{\"id\":\""
                + saleId + "\",\"status\":\"CONFIRMADA\"}}";

        wireMock.stubFor(patch(urlEqualTo("/api/v1/sales/" + saleId + "/confirm"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.patch()
                .uri("/api/v1/sales/{id}/confirm", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.status").isEqualTo("CONFIRMADA");
    }

    @Test
    @DisplayName("Gateway routes purchase creation to purchase-service")
    void purchaseCreateRoute() throws Exception {
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Map<String, Object> item = Map.of(
                "productId", productId.toString(),
                "productName", "Test Product",
                "quantity", 10,
                "unitPrice", 30.00
        );
        String purchaseJson = MAPPER.writeValueAsString(Map.of(
                "supplierId", supplierId.toString(),
                "items", java.util.List.of(item),
                "notes", "WireMock test purchase"
        ));
        UUID purchaseId = UUID.randomUUID();
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "CREATED",
                "data", Map.of(
                        "id", purchaseId.toString(),
                        "purchaseNumber", "OC-000001",
                        "status", "BORRADOR"
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/purchases"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/purchases")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(purchaseJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(purchaseId.toString())
                .jsonPath("$.data.status").isEqualTo("BORRADOR");
    }

    @Test
    @DisplayName("Gateway routes report request to report-service")
    void reportSalesRoute() throws Exception {
        String responseJson = "{\"success\":true,\"code\":\"SUCCESS\",\"data\":{"
                + "\"totalSales\":10,\"totalTransactions\":10,\"totalRevenue\":5000.00}}";

        wireMock.stubFor(get(urlEqualTo("/api/v1/reports/sales"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.get()
                .uri("/api/v1/reports/sales")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.totalRevenue").isEqualTo(5000.00);
    }

    @Test
    @DisplayName("Gateway rewrites /api/v1/dashboard to /api/v1/reports/dashboard")
    void dashboardRewriteRoute() throws Exception {
        String responseJson = "{\"success\":true,\"code\":\"SUCCESS\",\"data\":{"
                + "\"todayRevenue\":1500.00,\"todaySalesCount\":5}}";

        wireMock.stubFor(get(urlEqualTo("/api/v1/reports/dashboard"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.get()
                .uri("/api/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.todayRevenue").isEqualTo(1500.00);

        // Verify the rewrite happened by checking WireMock received request
        // to /api/v1/reports/dashboard (not /api/v1/dashboard)
        wireMock.verify(getRequestedFor(urlEqualTo("/api/v1/reports/dashboard")));
    }

    @Test
    @DisplayName("Gateway routes supplier creation to purchase-service")
    void suppliersCreateRoute() throws Exception {
        String supplierJson = MAPPER.writeValueAsString(Map.of(
                "codigo", "PROV-000001",
                "name", "Test Supplier",
                "nit", "900123456-7"
        ));
        UUID supplierId = UUID.randomUUID();
        String responseJson = MAPPER.writeValueAsString(Map.of(
                "success", true,
                "code", "CREATED",
                "data", Map.of(
                        "id", supplierId.toString(),
                        "codigo", "PROV-000001",
                        "name", "Test Supplier",
                        "status", "ACTIVO"
                )
        ));

        wireMock.stubFor(post(urlEqualTo("/api/v1/suppliers"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/suppliers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(supplierJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.id").isEqualTo(supplierId.toString());
    }

    @Test
    @DisplayName("Gateway routes get invoice by sale to sales-service")
    void invoiceBySaleRoute() throws Exception {
        UUID saleId = UUID.randomUUID();
        String responseJson = "{\"success\":true,\"code\":\"SUCCESS\",\"data\":{"
                + "\"id\":\"" + UUID.randomUUID() + "\","
                + "\"saleId\":\"" + saleId + "\","
                + "\"invoiceNumber\":1001,"
                + "\"total\":200.00}}";

        wireMock.stubFor(get(urlEqualTo("/api/v1/invoices/by-sale/" + saleId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.get()
                .uri("/api/v1/invoices/by-sale/{saleId}", saleId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.invoiceNumber").isEqualTo(1001);
    }

    @Test
    @DisplayName("Gateway routes user list to auth-service")
    void usuariosListRoute() throws Exception {
        String responseJson = "{\"success\":true,\"code\":\"SUCCESS\",\"data\":["
                + "{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"Admin\",\"email\":\"admin@test.com\",\"role\":\"ADMIN\",\"status\":\"ACTIVO\"}"
                + "]}";

        wireMock.stubFor(get(urlEqualTo("/api/v1/usuarios"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.get()
                .uri("/api/v1/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data[0].role").isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Gateway routes inventory entry to inventory-service")
    void inventoryEntryRoute() throws Exception {
        UUID productId = UUID.randomUUID();
        String entryJson = MAPPER.writeValueAsString(Map.of(
                "quantity", 20,
                "referenceType", "PURCHASE",
                "notes", "WireMock entry test"
        ));
        String responseJson = "{\"success\":true,\"code\":\"CREATED\",\"data\":{"
                + "\"id\":\"" + UUID.randomUUID() + "\","
                + "\"movementType\":\"ENTRADA\","
                + "\"quantity\":20}}";

        wireMock.stubFor(post(urlEqualTo("/api/v1/inventory/products/" + productId + "/entry"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseJson)));

        webTestClient.post()
                .uri("/api/v1/inventory/products/{productId}/entry", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(entryJson)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.movementType").isEqualTo("ENTRADA");
    }

    @Test
    @DisplayName("Gateway has all expected routes registered")
    void verifyAllRoutesRegistered() {
        var routes = routeLocator.getRoutes().collectList().block();
        Assertions.assertNotNull(routes);
        Assertions.assertTrue(routes.size() >= 7, "Expected at least 7 routes, got: " + routes.size());

        java.util.List<String> routeIds = routes.stream()
                .map(r -> r.getId())
                .toList();
        Assertions.assertTrue(routeIds.contains("auth-service"));
        Assertions.assertTrue(routeIds.contains("catalog-service"));
        Assertions.assertTrue(routeIds.contains("inventory-service"));
        Assertions.assertTrue(routeIds.contains("sales-service"));
        Assertions.assertTrue(routeIds.contains("purchase-service"));
        Assertions.assertTrue(routeIds.contains("report-service"));
        Assertions.assertTrue(routeIds.contains("dashboard-service"));

        System.out.println("All " + routes.size() + " routes registered: " + routeIds);
    }
}
