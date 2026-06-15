package com.axiserp.auth.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.axiserp.auth.application.service.JwtService;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthHeader_proceedsWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void nonBearerHeader_proceedsWithoutAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validToken_setsAuthentication() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();

        when(tokenBlacklistRepositoryPort.isTokenBlacklisted("valid-token")).thenReturn(false);
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.getUserIdFromToken("valid-token")).thenReturn("user-123");
        when(jwtService.getRoleFromToken("valid-token")).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user-123", auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void blacklistedToken_returns401() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked-token");
        var response = new MockHttpServletResponse();

        when(tokenBlacklistRepositoryPort.isTokenBlacklisted("revoked-token")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void expiredToken_doesNotSetAuth() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");
        var response = new MockHttpServletResponse();

        when(tokenBlacklistRepositoryPort.isTokenBlacklisted("expired-token")).thenReturn(false);
        when(jwtService.isTokenValid("expired-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedToken_clearsContextAndProceeds() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformed");
        var response = new MockHttpServletResponse();

        when(tokenBlacklistRepositoryPort.isTokenBlacklisted("malformed")).thenReturn(false);
        when(jwtService.isTokenValid("malformed")).thenThrow(new RuntimeException("Bad JWT"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidToken_isTokenValidFalse() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-sig-token");
        var response = new MockHttpServletResponse();

        when(tokenBlacklistRepositoryPort.isTokenBlacklisted("invalid-sig-token")).thenReturn(false);
        when(jwtService.isTokenValid("invalid-sig-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).getUserIdFromToken(any());
        verify(jwtService, never()).getRoleFromToken(any());
    }
}
