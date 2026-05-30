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

import com.axiserp.auth.application.dto.request.UpdateUserRequest;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.model.User.UserStatus;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UpdateUserUseCaseImpl updateUserUseCase;

    @Test
    @DisplayName("Should throw UserNotFoundException when user is ELIMINADO")
    void update_deletedUser() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("Test", "test@test.com", "ADMIN");
        User deletedUser = User.builder()
                .id(userId)
                .status(UserStatus.ELIMINADO)
                .deletedAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(deletedUser));

        assertThrows(UserNotFoundException.class,
                () -> updateUserUseCase.update(userId, request, UUID.randomUUID()));
    }
}
