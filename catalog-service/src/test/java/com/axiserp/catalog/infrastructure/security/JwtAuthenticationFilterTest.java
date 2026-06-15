package com.axiserp.catalog.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import jakarta.servlet.FilterChain;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

class JwtAuthenticationFilterTest {

    private static final String INTERNAL_API_KEY = "test-internal-key-123";
    private static final String JWKS_URL = "https://supabase.test/.well-known/jwks.json";

    private static final SecretKey HS256_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private static final String SECRET_B64 = Base64.getEncoder().encodeToString(HS256_KEY.getEncoded());
    private static final String WRONG_SECRET_B64 = Base64.getEncoder().encodeToString(
            Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());

    private static final Map<String, Object> JWKS_ES256 = Map.of("keys", List.of(Map.of(
            "kid", "kid-es256",
            "alg", "ES256",
            "kty", "EC",
            "crv", "P-256",
            "x", "MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4",
            "y", "4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM"
    )));

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

        filter = new JwtAuthenticationFilter(restClientBuilder, JWKS_URL, INTERNAL_API_KEY, SECRET_B64);
    }

    private String createEs256Token() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"ES256\",\"kid\":\"kid-es256\"}".getBytes()) + "."
                + Base64.getUrlEncoder().withoutPadding()
                        .encodeToString("{\"sub\":\"user-123\"}".getBytes()) + "."
                + "fake-signature";
    }

    private String createHs256Token(SecretKey key, String kid) {
        var builder = Jwts.builder().subject("user-123").claim("role", "ADMIN");
        if (kid != null) {
            builder = builder.header().add("kid", kid).and();
        }
        return builder.signWith(key).compact();
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
    void invalidInternalApiKey_doesNotAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "wrong-key");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingAuthHeader_proceeds() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validHS256Token_setsAuthentication() throws Exception {
        String token = createHs256Token(HS256_KEY, null);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user-123", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void hs256WrongSecret_fallsbackToJwks() throws Exception {
        String token = createHs256Token(Keys.hmacShaKeyFor(Decoders.BASE64.decode(WRONG_SECRET_B64)), null);
        when(responseSpec.body(Map.class)).thenReturn(JWKS_ES256);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(restClient).get();
    }

    @Test
    void jwksCacheHit_doesNotRefetch() throws Exception {
        when(responseSpec.body(Map.class)).thenReturn(JWKS_ES256);

        String token = createEs256Token();

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
        when(responseSpec.body(Map.class)).thenReturn(JWKS_ES256);

        String token = createEs256Token();
        var request1 = new MockHttpServletRequest();
        request1.addHeader("Authorization", "Bearer " + token);
        var response1 = new MockHttpServletResponse();

        filter.doFilterInternal(request1, response1, filterChain);
        verify(restClient, times(1)).get();

        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(restClientBuilder, JWKS_URL, INTERNAL_API_KEY, SECRET_B64) {
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
        lenient().when(responseSpec.body(Map.class)).thenReturn(JWKS_ES256);

        var request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Bearer " + token);
        var response2 = new MockHttpServletResponse();

        filter.doFilterInternal(request2, response2, filterChain);
        verify(restClient, times(2)).get();
    }

    @Test
    void malformedToken_clearsContext() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-valid-jwt");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwksReturnsEmptyKeys_proceedsWithoutAuth() throws Exception {
        when(responseSpec.body(Map.class)).thenReturn(Map.of("keys", List.of()));

        String token = createEs256Token();
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noJwtSecret_skipsHs256AndGoesToJwks() {
        filter = new JwtAuthenticationFilter(restClientBuilder, JWKS_URL, INTERNAL_API_KEY, "");
        lenient().when(responseSpec.body(Map.class)).thenReturn(Map.of("keys", List.of()));

        try {
            var request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer some-token");
            var response = new MockHttpServletResponse();

            filter.doFilterInternal(request, response, filterChain);
            verify(restClient).get();
        } catch (Exception e) {
            fail("Should not throw", e);
        }
    }

    @Test
    void hs256TokenWithoutRole_fallsBackToVENDEDOR() throws Exception {
        String token = Jwts.builder()
                .subject("user-456")
                .signWith(HS256_KEY)
                .compact();

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user-456", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR")));
    }
}
