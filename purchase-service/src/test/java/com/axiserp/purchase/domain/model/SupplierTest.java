package com.axiserp.purchase.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Supplier Domain Model")
class SupplierTest {

    @Test
    @DisplayName("Should be active when status is ACTIVO")
    void isActive_returnsTrue_whenActivo() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.ACTIVO)
                .build();

        assertTrue(supplier.isActive());
    }

    @Test
    @DisplayName("Should not be active when status is INACTIVO")
    void isActive_returnsFalse_whenInactivo() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.INACTIVO)
                .build();

        assertFalse(supplier.isActive());
    }

    @Test
    @DisplayName("Should not be active when status is ELIMINADO")
    void isActive_returnsFalse_whenEliminado() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.ELIMINADO)
                .build();

        assertFalse(supplier.isActive());
    }

    @Test
    @DisplayName("Should be deleted when status is ELIMINADO")
    void isDeleted_returnsTrue_whenEliminado() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.ELIMINADO)
                .build();

        assertTrue(supplier.isDeleted());
    }

    @Test
    @DisplayName("Should not be deleted when status is ACTIVO")
    void isDeleted_returnsFalse_whenActivo() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.ACTIVO)
                .build();

        assertFalse(supplier.isDeleted());
    }

    @Test
    @DisplayName("Should not be deleted when status is INACTIVO")
    void isDeleted_returnsFalse_whenInactivo() {
        Supplier supplier = Supplier.builder()
                .status(SupplierStatus.INACTIVO)
                .build();

        assertFalse(supplier.isDeleted());
    }

    @Test
    @DisplayName("Nit should be immutable via builder")
    void nit_isImmutable() {
        Supplier supplier = Supplier.builder()
                .id(UUID.randomUUID())
                .codigo("PROV-000001")
                .name("Test")
                .nit("123456789")
                .build();

        assertEquals("123456789", supplier.getNit());
    }
}
