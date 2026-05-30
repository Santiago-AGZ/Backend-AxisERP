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
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.domain.exception.UserNotFoundException;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @InjectMocks
    private GetUserUseCaseImpl getUserUseCase;

    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getById_success() {
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@axiserp.com")
                .roleId(roleId)
                .status(User.UserStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .build();

        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepositoryPort.findById(roleId)).thenReturn(Optional.of(role));

        UserResponse response = getUserUseCase.getById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@axiserp.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ACTIVO", response.getStatus());
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found")
    void getById_notFound() {
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> getUserUseCase.getById(userId));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user is ELIMINADO")
    void getById_deletedUser() {
        User deletedUser = User.builder()
                .id(userId)
                .status(User.UserStatus.ELIMINADO)
                .deletedAt(java.time.LocalDateTime.now())
                .build();

        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(deletedUser));

        assertThrows(UserNotFoundException.class, () -> getUserUseCase.getById(userId));
    }
}
