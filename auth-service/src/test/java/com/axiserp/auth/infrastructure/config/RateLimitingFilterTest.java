package com.axiserp.auth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void normalRequest_proceeds() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void rateLimitExceeded_returns429() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        for (int i = 0; i < 100; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        var rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, filterChain);

        assertEquals(429, rateLimitedResponse.getStatus());
        assertEquals("60", rateLimitedResponse.getHeader("Retry-After"));
        assertTrue(rateLimitedResponse.getContentAsString().contains("Too Many Requests"));
    }

    @Test
    void differentIps_haveIndependentLimits() throws Exception {
        var request1 = new MockHttpServletRequest();
        request1.setRemoteAddr("10.0.0.2");

        var request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("10.0.0.3");

        for (int i = 0; i < 100; i++) {
            if (i > 0) {
                var r1 = new MockHttpServletResponse();
                filter.doFilterInternal(request1, r1, filterChain);
            }
        }

        var response2 = new MockHttpServletResponse();
        filter.doFilterInternal(request2, response2, filterChain);

        assertEquals(200, response2.getStatus());
    }

    @Test
    void rateLimitedResponse_hasRetryAfterHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.4");
        var response = new MockHttpServletResponse();

        for (int i = 0; i < 100; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        var rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, filterChain);

        assertNotNull(rateLimitedResponse.getHeader("Retry-After"));
        assertEquals("60", rateLimitedResponse.getHeader("Retry-After"));
    }

    @Test
    void xForwardedForUsedAsClientIp() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        request.setRemoteAddr("10.0.0.1");

        for (int i = 0; i < 100; i++) {
            var r = new MockHttpServletResponse();
            filter.doFilterInternal(request, r, filterChain);
        }

        var rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, filterChain);

        assertEquals(429, rateLimitedResponse.getStatus());
    }

    @Test
    void actuatorPath_skipsRateLimiting() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");
        request.setRemoteAddr("10.0.0.5");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }
}
