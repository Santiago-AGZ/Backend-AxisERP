package com.axiserp.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
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

import com.axiserp.auth.application.dto.request.CreateUserRequest;
import com.axiserp.auth.application.dto.response.UserResponse;
import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.Role;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.exception.DuplicateEmailException;
import com.axiserp.auth.domain.exception.UserInactiveException;
import com.axiserp.auth.ports.output.RoleRepositoryPort;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.SupabaseUser;
import com.axiserp.auth.ports.output.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private AuditService auditService;

    @Mock
    private SupabaseAuthPort supabaseAuthPort;

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
    @DisplayName("Should create user successfully via Supabase Auth")
    void create_success() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "ADMIN");
        Role role = Role.builder().id(roleId).name("ADMIN").description("Admin role").build();
        UUID supabaseId = UUID.randomUUID();
        SupabaseUser supabaseUser = new SupabaseUser(supabaseId, "test@axiserp.com", Instant.now());
        User savedUser = User.builder()
                .id(supabaseId)
                .name("Test User")
                .email("test@axiserp.com")
                .roleId(roleId)
                .status(User.UserStatus.PENDIENTE)
                .createdBy(adminId)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(userRepositoryPort.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).status(User.UserStatus.ACTIVO).build()));
        when(roleRepositoryPort.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(supabaseAuthPort.createUser("test@axiserp.com", "ADMIN", "Test User", adminId))
                .thenReturn(supabaseUser);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = createUserUseCase.create(request, adminId);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@axiserp.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertEquals("PENDIENTE", response.getStatus());
        verify(supabaseAuthPort).createUser("test@axiserp.com", "ADMIN", "Test User", adminId);
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email exists")
    void create_duplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("Test User", "existing@axiserp.com", "ADMIN");

        when(userRepositoryPort.existsByEmail("existing@axiserp.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> createUserUseCase.create(request, adminId));
        verify(userRepositoryPort, never()).save(any());
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when role is invalid")
    void create_invalidRole() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "INVALID");

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(userRepositoryPort.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).status(User.UserStatus.ACTIVO).build()));
        when(roleRepositoryPort.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> createUserUseCase.create(request, adminId));
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw UserInactiveException when admin is inactive")
    void create_adminInactive() {
        CreateUserRequest request = new CreateUserRequest("Test User", "test@axiserp.com", "ADMIN");

        when(userRepositoryPort.existsByEmail("test@axiserp.com")).thenReturn(false);
        when(userRepositoryPort.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).status(User.UserStatus.INACTIVO).build()));

        assertThrows(UserInactiveException.class, () -> createUserUseCase.create(request, adminId));
        verify(supabaseAuthPort, never()).createUser(any(), any(), any(), any());
    }
}
