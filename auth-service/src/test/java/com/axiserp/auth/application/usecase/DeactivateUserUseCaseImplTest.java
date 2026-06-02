package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.RefreshTokenRepositoryPort;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.TokenBlacklistRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class DeactivateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @Mock
    private TokenBlacklistRepositoryPort tokenBlacklistRepositoryPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeactivateUserUseCaseImpl deactivateUserUseCase;

    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void deactivate_success() {
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$hashed")
                .roleId(roleId)
                .status(User.UserStatus.ACTIVO)
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepositoryPort.findById(roleId)).thenReturn(Optional.of(role));
        when(refreshTokenRepositoryPort.findActiveByUserId(userId)).thenReturn(java.util.List.of());
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return User.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .email(saved.getEmail())
                    .passwordHash(saved.getPasswordHash())
                    .roleId(saved.getRoleId())
                    .status(User.UserStatus.INACTIVO)
                    .createdBy(saved.getCreatedBy())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
        });

        UserResponse response = deactivateUserUseCase.deactivate(userId);

        assertNotNull(response);
        assertEquals("INACTIVO", response.getStatus());
        verify(userRepositoryPort).save(argThat(u -> u.getStatus() == User.UserStatus.INACTIVO));
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found")
    void deactivate_notFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> deactivateUserUseCase.deactivate(userId));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user already inactive")
    void deactivate_alreadyInactive() {
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$hashed")
                .roleId(roleId)
                .status(User.UserStatus.INACTIVO)
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> deactivateUserUseCase.deactivate(userId));
    }
}
