package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.application.service.ReauthenticationValidator;
import com.axiserp.auth.application.service.RefreshTokenService;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class DeactivateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private AuditService auditService;

    @Mock
    private ReauthenticationValidator reauthenticationValidator;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private DeactivateUserUseCaseImpl deactivateUserUseCase;

    @Test
    @DisplayName("[R15] Should deactivate user and revoke refresh tokens")
    void deactivate_success_revokesTokens() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User activeUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .status(UserStatus.ACTIVO)
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(activeUser));
        doNothing().when(reauthenticationValidator).validate(adminId, "password");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roleRepositoryPort.findById(any())).thenReturn(Optional.empty());

        deactivateUserUseCase.deactivate(userId, adminId, "password");

        verify(refreshTokenService).revokeByUserId(userId);
        verify(auditService).log(any(), eq("USER"), eq(userId), isNull(), isNull(), any(), isNull(), isNull());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user is already INACTIVO")
    void deactivate_alreadyInactive() {
        UUID userId = UUID.randomUUID();
        User inactiveUser = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVO)
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(inactiveUser));
        doNothing().when(reauthenticationValidator).validate(any(), anyString());

        assertThrows(IllegalStateException.class,
                () -> deactivateUserUseCase.deactivate(userId, UUID.randomUUID(), "password"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user is already ELIMINADO")
    void deactivate_alreadyDeleted() {
        User deletedUser = User.builder()
                .id(UUID.randomUUID())
                .status(UserStatus.ELIMINADO)
                .deletedAt(LocalDateTime.now())
                .build();
        when(userRepositoryPort.findById(deletedUser.getId())).thenReturn(Optional.of(deletedUser));

        doNothing().when(reauthenticationValidator).validate(any(), anyString());

        assertThrows(IllegalStateException.class,
                () -> deactivateUserUseCase.deactivate(deletedUser.getId(), UUID.randomUUID(), "password"));
    }
}
