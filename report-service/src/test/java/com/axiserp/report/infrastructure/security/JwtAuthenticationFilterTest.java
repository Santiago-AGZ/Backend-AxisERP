package com.axiserp.report.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import jakarta.servlet.FilterChain;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

class JwtAuthenticationFilterTest {

    private static final String INTERNAL_API_KEY = "report-internal-key-777";
    private static final String JWKS_URL = "https://supabase.test/.well-known/jwks.json";

    private RestClient.Builder restClientBuilder;
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;
    private FilterChain filterChain;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        restClientBuilder = mock(RestClient.Builder.class);
        restClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        filterChain = mock(FilterChain.class);

        lenient().when(restClientBuilder.build()).thenReturn(restClient);
        lenient().when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        filter = new JwtAuthenticationFilter(restClientBuilder, JWKS_URL, INTERNAL_API_KEY);
    }

    @Test
    void internalApiKey_setsAdminAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", INTERNAL_API_KEY);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("00000000-0000-0000-0000-000000000000", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void wrongInternalApiKey_doesNotAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "wrong-key");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noAuthHeader_proceeds() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void es256Token_fetchesJwksKeys() throws Exception {
        Map<String, Object> jwksResponse = createJwksResponse("kid-es256", "ES256");
        when(responseSpec.body(Map.class)).thenReturn(jwksResponse);

        String token = createTokenWithHeader("ES256", "kid-es256");

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(JWKS_URL);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void jwksCacheHit_doesNotRefetch() throws Exception {
        Map<String, Object> jwksResponse = createJwksResponse("kid-es256", "ES256");
        when(responseSpec.body(Map.class)).thenReturn(jwksResponse);

        String token = createTokenWithHeader("ES256", "kid-es256");

        var request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "Bearer " + token);
        var response1 = new MockHttpServletResponse();

        filter.doFilterInternal(request1, response1, filterChain);
        verify(restClient, times(1)).get();

        SecurityContextHolder.clearContext();

        var request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Bearer " + token);
        var response2 = new MockHttpServletResponse();

        filter.doFilterInternal(request2, response2, filterChain);
        verify(restClient, times(1)).get();
    }

    @Test
    void jwksCacheTtlExpiry_refetches() throws Exception {
        Map<String, Object> jwksResponse = createJwksResponse("kid-es256", "ES256");
        when(responseSpec.body(Map.class)).thenReturn(jwksResponse);

        String token = createTokenWithHeader("ES256", "kid-es256");
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);
        verify(restClient, times(1)).get();

        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(restClientBuilder, JWKS_URL, INTERNAL_API_KEY) {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req,
                    jakarta.servlet.http.HttpServletResponse res, FilterChain chain)
                    throws jakarta.servlet.ServletException, java.io.IOException {
                try {
                    var field = JwtAuthenticationFilter.class.getDeclaredField("cachedKeyTimestamp");
                    field.setAccessible(true);
                    field.set(this, Instant.now().minus(Duration.ofMinutes(20)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                super.doFilterInternal(req, res, chain);
            }
        };
        lenient().when(restClientBuilder.build()).thenReturn(restClient);
        lenient().when(responseSpec.body(Map.class)).thenReturn(jwksResponse);

        var request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Bearer " + token);
        var response2 = new MockHttpServletResponse();

        filter.doFilterInternal(request2, response2, filterChain);
        verify(restClient, times(2)).get();
    }

    @Test
    void jwksReturnsNullKey_proceedsWithoutAuth() throws Exception {
        when(responseSpec.body(Map.class)).thenReturn(Map.of("keys", List.of()));
        String token = createTokenWithHeader("ES256", "unknown-kid");

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwksFetchFails_proceedsWithoutAuth() throws Exception {
        when(responseSpec.body(Map.class)).thenThrow(new RuntimeException("Connection refused"));
        String token = createTokenWithHeader("ES256", "kid-es256");

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedToken_clearsContextAndProceeds() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid....jwt");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private String createTokenWithHeader(String alg, String kid) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"alg\":\"" + alg + "\",\"kid\":\"" + kid + "\"}").getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"user-123\"}".getBytes());
        return header + "." + payload + ".fake-signature";
    }

    private Map<String, Object> createJwksResponse(String kid, String alg) {
        return Map.of("keys", List.of(Map.of(
                "kid", kid,
                "alg", alg,
                "kty", "EC",
                "crv", "P-256",
                "x", "MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4",
                "y", "4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM"
        )));
    }
}
