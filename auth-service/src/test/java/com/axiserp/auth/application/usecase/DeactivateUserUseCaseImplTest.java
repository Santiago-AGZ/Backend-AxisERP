package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private DeactivateUserUseCaseImpl deactivateUserUseCase;

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
