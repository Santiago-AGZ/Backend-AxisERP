package com.axiserp.purchase.application.usecase;

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

import com.axiserp.purchase.application.dto.request.CreateSupplierRequest;
import com.axiserp.purchase.application.dto.response.SupplierResponse;
import com.axiserp.purchase.domain.exception.DuplicateNitException;
import com.axiserp.purchase.domain.model.Supplier;
import com.axiserp.purchase.domain.model.SupplierStatus;
import com.axiserp.purchase.ports.output.SupplierRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSupplierUseCaseImpl")
class CreateSupplierUseCaseImplTest {

    @Mock
    private SupplierRepositoryPort supplierRepositoryPort;

    private CreateSupplierUseCaseImpl createSupplierUseCase;

    private CreateSupplierRequest request;
    private Supplier savedSupplier;

    @BeforeEach
    void setUp() {
        createSupplierUseCase = new CreateSupplierUseCaseImpl(supplierRepositoryPort);

        request = CreateSupplierRequest.builder()
                .codigo("PROV-000001")
                .name("Proveedor Test")
                .nit("123456789")
                .phone("555-0100")
                .email("test@proveedor.com")
                .address("Calle 123")
                .build();

        savedSupplier = Supplier.builder()
                .id(UUID.randomUUID())
                .codigo("PROV-000001")
                .name("Proveedor Test")
                .nit("123456789")
                .phone("555-0100")
                .email("test@proveedor.com")
                .address("Calle 123")
                .status(SupplierStatus.ACTIVO)
                .build();
    }

    @Test
    @DisplayName("Should create supplier successfully")
    void execute_success() {
        when(supplierRepositoryPort.existsByCodigo(anyString())).thenReturn(false);
        when(supplierRepositoryPort.existsByNit(anyString())).thenReturn(false);
        when(supplierRepositoryPort.save(any(Supplier.class))).thenReturn(savedSupplier);

        SupplierResponse response = createSupplierUseCase.execute(request);

        assertNotNull(response);
        assertEquals("PROV-000001", response.getCodigo());
        assertEquals("Proveedor Test", response.getName());
        assertEquals("123456789", response.getNit());
        assertEquals(SupplierStatus.ACTIVO, response.getStatus());
        verify(supplierRepositoryPort).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw when codigo already exists")
    void execute_duplicateCodigo_throws() {
        when(supplierRepositoryPort.existsByCodigo(request.getCodigo())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> createSupplierUseCase.execute(request));
        verify(supplierRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateNitException when nit already exists")
    void execute_duplicateNit_throws() {
        when(supplierRepositoryPort.existsByCodigo(anyString())).thenReturn(false);
        when(supplierRepositoryPort.existsByNit(request.getNit())).thenReturn(true);

        assertThrows(DuplicateNitException.class, () -> createSupplierUseCase.execute(request));
        verify(supplierRepositoryPort, never()).save(any());
    }
}
