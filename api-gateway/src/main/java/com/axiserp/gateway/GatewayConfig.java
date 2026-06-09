package com.axiserp.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class GatewayConfig {

    @Value("${AUTH_SERVICE_URL:http://auth-service:8081}")
    private String authServiceUrl;

    @Value("${CATALOG_SERVICE_URL:http://catalog-service:8082}")
    private String catalogServiceUrl;

    @Value("${INVENTORY_SERVICE_URL:http://inventory-service:8083}")
    private String inventoryServiceUrl;

    @Value("${SALES_SERVICE_URL:http://sales-service:8084}")
    private String salesServiceUrl;

    @Value("${PURCHASE_SERVICE_URL:http://purchase-service:8086}")
    private String purchaseServiceUrl;

    @Value("${REPORT_SERVICE_URL:http://report-service:8085}")
    private String reportServiceUrl;

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://frontend-axis-erp.vercel.app,https://*.vercel.app}")
    private List<String> allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

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
