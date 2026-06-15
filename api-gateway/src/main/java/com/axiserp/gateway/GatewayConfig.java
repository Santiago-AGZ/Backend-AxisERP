package com.axiserp.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class GatewayConfig {

    // La seguridad NO se implementa en el gateway.
    // Cada microservicio protege sus propios endpoints via JWT, OAuth2 Resource Server,
    // InternalApiKeyFilter, y Spring Security Method Security.

    @Value("${auth-service-url:http://auth-service:8081}")
    private String authServiceUrl;

    @Value("${catalog-service-url:http://catalog-service:8082}")
    private String catalogServiceUrl;

    @Value("${inventory-service-url:http://inventory-service:8083}")
    private String inventoryServiceUrl;

    @Value("${sales-service-url:http://sales-service:8084}")
    private String salesServiceUrl;

    @Value("${purchase-service-url:http://purchase-service:8086}")
    private String purchaseServiceUrl;

    @Value("${report-service-url:http://report-service:8085}")
    private String reportServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r
                .path("/api/v1/auth/**", "/api/v1/usuarios/**", "/api/v1/audit-log/**",
                      "/swagger-ui/**", "/v3/api-docs/**")
                .uri(authServiceUrl))
            .route("catalog-service", r -> r
                .path("/api/v1/productos/**", "/api/v1/categorias/**")
                .uri(catalogServiceUrl))
            .route("inventory-service", r -> r
                .path("/api/v1/inventory/**")
                .uri(inventoryServiceUrl))
            .route("sales-service", r -> r
                .path("/api/v1/sales/**", "/api/v1/customers/**", "/api/v1/invoices/**")
                .uri(salesServiceUrl))
            .route("purchase-service", r -> r
                .path("/api/v1/purchases/**", "/api/v1/suppliers/**")
                .uri(purchaseServiceUrl))
            .route("report-service", r -> r
                .path("/api/v1/reports/**")
                .uri(reportServiceUrl))
            .route("dashboard-service", r -> r
                .path("/api/v1/dashboard")
                .filters(f -> f.rewritePath("/api/v1/dashboard", "/api/v1/reports/dashboard"))
                .uri(reportServiceUrl))
            .build();
    }
}
