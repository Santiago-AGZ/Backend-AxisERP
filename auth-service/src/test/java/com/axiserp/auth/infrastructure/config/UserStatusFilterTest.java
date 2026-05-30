package com.axiserp.auth.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UserStatusFilterTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RoleRepositoryPort roleRepository;

    @InjectMocks
    private UserStatusFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsActiveUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User activeUser = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        FilterChain chain = mock(FilterChain.class);
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void blocksInactiveUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User inactiveUser = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVO)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserInactiveException.class, () ->
                filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                        (req, res) -> {}));
    }

    @Test
    void blocksDeletedUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "test@axiserp.com")
                .build();
        setAuthentication(userId, jwt);

        User deletedUser = User.builder()
                .id(userId)
                .status(UserStatus.ELIMINADO)
                .deletedAt(java.time.LocalDateTime.now())
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedUser));

        assertThrows(UserInactiveException.class, () ->
                filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                        (req, res) -> {}));
    }

    @Test
    void provisionsUserWhenNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email", "new@axiserp.com")
                .claim("user_metadata", java.util.Map.of("name", "New User"))
                .build();
        setAuthentication(userId, jwt);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        User saved = User.builder().id(userId).name("New User").email("new@axiserp.com").status(UserStatus.ACTIVO).build();
        when(userRepository.save(any())).thenReturn(saved);

        assertDoesNotThrow(() ->
            filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                    (req, res) -> {}));

        verify(userRepository).save(any());
    }

    @Test
    void skipsActuatorPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> {});

        // Should not throw despite no auth since actuator paths are skipped
    }

    private void setAuthentication(UUID userId, Jwt jwt) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, List.of()));
    }
}
