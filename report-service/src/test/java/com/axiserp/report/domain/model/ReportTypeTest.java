package com.axiserp.report.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReportType Domain")
class ReportTypeTest {

    @Test
    @DisplayName("should have correct enum values")
    void enumValues() {
        assertEquals(5, ReportType.values().length);
        assertNotNull(ReportType.valueOf("DAILY_SALES"));
        assertNotNull(ReportType.valueOf("INVENTORY_STATUS"));
        assertNotNull(ReportType.valueOf("TOP_PRODUCTS"));
        assertNotNull(ReportType.valueOf("CUSTOMER_FREQUENCY"));
        assertNotNull(ReportType.valueOf("DASHBOARD_SUMMARY"));
    }

    @Test
    @DisplayName("should return correct display names")
    void displayNames() {
        assertEquals("Ventas del Día", ReportType.DAILY_SALES.getDisplayName());
        assertEquals("Estado del Inventario", ReportType.INVENTORY_STATUS.getDisplayName());
        assertEquals("Productos Más Vendidos", ReportType.TOP_PRODUCTS.getDisplayName());
        assertEquals("Frecuencia de Clientes", ReportType.CUSTOMER_FREQUENCY.getDisplayName());
        assertEquals("Resumen del Dashboard", ReportType.DASHBOARD_SUMMARY.getDisplayName());
    }
}
