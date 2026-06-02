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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CreateUserUseCaseImpl createUserUseCase;

    private UUID adminId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create user successfully")
    void create_success() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "Test@1234", "ADMIN");
        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@axiserp.com")
                .passwordHash("$2a$10$hashed")
                .roleId(roleId)
                .status(User.UserStatus.ACTIVO)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(roleRepositoryPort.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Test@1234")).thenReturn("$2a$10$hashed");
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = createUserUseCase.create(request, adminId);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@axiserp.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ACTIVO", response.getStatus());
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email exists")
    void create_duplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("Test User", "existing@axiserp.com", "Test@1234", "ADMIN");

        when(userRepositoryPort.existsByEmail("existing@axiserp.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> createUserUseCase.create(request, adminId));
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when role is invalid")
    void create_invalidRole() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "Test@1234", "INVALID");

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(roleRepositoryPort.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> createUserUseCase.create(request, adminId));
    }
}
