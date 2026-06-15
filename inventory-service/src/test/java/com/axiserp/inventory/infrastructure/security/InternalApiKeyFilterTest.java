package com.axiserp.inventory.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_API_KEY = "inventory-internal-key-999";

    private InternalApiKeyFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new InternalApiKeyFilter();
        ReflectionTestUtils.setField(filter, "internalApiKey", INTERNAL_API_KEY);
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validApiKey_setsAdminAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", INTERNAL_API_KEY);
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("00000000-0000-0000-0000-000000000000", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void invalidApiKey_doesNotAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "wrong-key");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingApiKey_proceedsWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void emptyApiKeyConfig_doesNotAuth() throws Exception {
        var filterWithEmptyKey = new InternalApiKeyFilter();
        ReflectionTestUtils.setField(filterWithEmptyKey, "internalApiKey", "");

        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "some-key");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filterWithEmptyKey.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void differentKeyForDifferentService() throws Exception {
        String differentServiceKey = "catalog-internal-key-123";
        var filter2 = new InternalApiKeyFilter();
        ReflectionTestUtils.setField(filter2, "internalApiKey", differentServiceKey);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", INTERNAL_API_KEY);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter2.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
