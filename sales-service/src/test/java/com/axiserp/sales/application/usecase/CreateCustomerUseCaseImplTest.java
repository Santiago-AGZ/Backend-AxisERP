package com.axiserp.sales.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.sales.application.dto.request.CreateCustomerRequest;
import com.axiserp.sales.domain.exception.DuplicateDocumentException;
import com.axiserp.sales.domain.model.Customer;
import com.axiserp.sales.domain.model.CustomerStatus;
import com.axiserp.sales.ports.output.CustomerRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCustomerUseCaseImpl")
class CreateCustomerUseCaseImplTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private CreateCustomerUseCaseImpl createCustomerUseCase;
    private UUID createdBy;
    private CreateCustomerRequest request;

    @BeforeEach
    void setUp() {
        createCustomerUseCase = new CreateCustomerUseCaseImpl(customerRepositoryPort);
        createdBy = UUID.randomUUID();

        request = CreateCustomerRequest.builder()
                .codigo("CLI-000001")
                .name("Test Customer")
                .documentType("NIT")
                .documentNumber("123456789")
                .email("test@example.com")
                .phone("555-0100")
                .address("Calle 123")
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void create_success() {
        when(customerRepositoryPort.existsByCodigo("CLI-000001")).thenReturn(false);
        when(customerRepositoryPort.existsByDocumentNumber("123456789")).thenReturn(false);
        when(customerRepositoryPort.existsByEmail("test@example.com")).thenReturn(false);
        when(customerRepositoryPort.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return Customer.builder()
                    .id(UUID.randomUUID())
                    .codigo(c.getCodigo())
                    .name(c.getName())
                    .documentType(c.getDocumentType())
                    .documentNumber(c.getDocumentNumber())
                    .email(c.getEmail())
                    .phone(c.getPhone())
                    .address(c.getAddress())
                    .status(CustomerStatus.ACTIVO)
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build();
        });

        var response = createCustomerUseCase.create(request, createdBy);

        assertNotNull(response);
        assertEquals("CLI-000001", response.getCodigo());
        assertEquals("ACTIVO", response.getStatus());
        verify(customerRepositoryPort).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when codigo exists")
    void create_duplicateCodigo_throws() {
        when(customerRepositoryPort.existsByCodigo("CLI-000001")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> createCustomerUseCase.create(request, createdBy));
        verify(customerRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateDocumentException when document exists")
    void create_duplicateDocument_throws() {
        when(customerRepositoryPort.existsByCodigo("CLI-000001")).thenReturn(false);
        when(customerRepositoryPort.existsByDocumentNumber("123456789")).thenReturn(true);

        assertThrows(DuplicateDocumentException.class, () -> createCustomerUseCase.create(request, createdBy));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when email exists")
    void create_duplicateEmail_throws() {
        when(customerRepositoryPort.existsByCodigo("CLI-000001")).thenReturn(false);
        when(customerRepositoryPort.existsByDocumentNumber("123456789")).thenReturn(false);
        when(customerRepositoryPort.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> createCustomerUseCase.create(request, createdBy));
    }

    @Test
    @DisplayName("Should create customer without email")
    void create_withoutEmail_success() {
        request.setEmail(null);

        when(customerRepositoryPort.existsByCodigo("CLI-000001")).thenReturn(false);
        when(customerRepositoryPort.existsByDocumentNumber("123456789")).thenReturn(false);
        when(customerRepositoryPort.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return Customer.builder()
                    .id(UUID.randomUUID())
                    .codigo(c.getCodigo())
                    .name(c.getName())
                    .status(CustomerStatus.ACTIVO)
                    .build();
        });

        var response = createCustomerUseCase.create(request, createdBy);

        assertNotNull(response);
        assertEquals("CLI-000001", response.getCodigo());
    }
}
