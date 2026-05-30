package com.axiserp.auth.infrastructure.config;

import static org.mockito.Mockito.*;

import java.time.Instant;
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

import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class FirstLoginFilterTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private FirstLoginFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPromotePendingToActivoWhenEmailConfirmed() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email_confirmed_at", Instant.now().toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        User pendingUser = User.builder()
                .id(userId)
                .status(UserStatus.PENDIENTE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(pendingUser));

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository).findById(userId);
        verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVO));
    }

    @Test
    void shouldNotUpdateWhenStatusIsAlreadyActivo() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .claim("email_confirmed_at", Instant.now().toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        User activoUser = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(activoUser));

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateWhenEmailNotConfirmed() throws Exception {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", userId.toString())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId.toString(), jwt, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateWhenNoAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> {});

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }
}
