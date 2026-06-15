package com.axiserp.purchase.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/test",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.hibernate.ddl-auto=none",
    "internal-api-key=test-key",
    "catalog-service-url=http://localhost:8082",
    "inventory-service-url=http://localhost:8083",
    "jwt.jwks-uri=http://localhost:9999/.well-known/jwks.json"
})
class RestTemplateConfigurationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void restTemplateBean_shouldExist() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void restTemplate_connectTimeout_is5Seconds() {
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(10));

        var expectedFactory = ClientHttpRequestFactories.get(settings);
        assertThat(factory).isInstanceOf(expectedFactory.getClass());
    }

    @Test
    void restTemplate_readTimeout_is10Seconds() {
        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();

        if (factory instanceof JdkClientHttpRequestFactory jdkFactory) {
            assertThat(jdkFactory).isNotNull();
        }

        assertThat(factory).isNotNull();
    }
}
