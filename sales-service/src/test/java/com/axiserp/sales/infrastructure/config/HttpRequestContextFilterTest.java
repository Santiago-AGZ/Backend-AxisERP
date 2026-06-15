package com.axiserp.sales.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.axiserp.sales.application.shared.RequestContext;

import jakarta.servlet.FilterChain;

class HttpRequestContextFilterTest {

    private HttpRequestContextFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HttpRequestContextFilter();
        RequestContext.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void extractsXForwardedForHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("203.0.113.5", RequestContext.getIpAddress());
        });
    }

    @Test
    void fallsBackToXRealIp() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.2");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("198.51.100.2", RequestContext.getIpAddress());
        });
    }

    @Test
    void fallsBackToRemoteAddr() throws Exception {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("10.0.0.1", RequestContext.getIpAddress());
        });
    }

    @Test
    void extractsUserAgentHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 TestBrowser");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("Mozilla/5.0 TestBrowser", RequestContext.getUserAgent());
        });
    }

    @Test
    void xForwardedForPrecedesXRealIp() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        request.addHeader("X-Real-IP", "198.51.100.2");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("203.0.113.5", RequestContext.getIpAddress());
        });
    }

    @Test
    void xRealIpPrecedesRemoteAddr() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.2");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("198.51.100.2", RequestContext.getIpAddress());
        });
    }

    @Test
    void handlesCommaSeparatedXForwardedFor() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1, 192.168.1.1");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("203.0.113.5", RequestContext.getIpAddress());
        });
    }

    @Test
    void handlesUnknownXForwardedFor() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("X-Real-IP", "198.51.100.2");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertEquals("198.51.100.2", RequestContext.getIpAddress());
        });
    }

    @Test
    void clearsThreadLocalAfterRequest() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertNotNull(RequestContext.getIpAddress());
        });

        assertNull(RequestContext.getIpAddress());
        assertNull(RequestContext.getUserAgent());
    }

    @Test
    void clearsThreadLocalEvenOnException() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        var response = new MockHttpServletResponse();

        assertThrows(RuntimeException.class, () ->
            filter.doFilter(request, response, (req, res) -> {
                throw new RuntimeException("Chain failed");
            })
        );

        assertNull(RequestContext.getIpAddress());
        assertNull(RequestContext.getUserAgent());
    }
}
