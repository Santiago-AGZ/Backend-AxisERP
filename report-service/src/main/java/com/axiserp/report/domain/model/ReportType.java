package com.axiserp.report.domain.model;

public enum ReportType {
    DAILY_SALES("Ventas del Día"),
    INVENTORY_STATUS("Estado del Inventario"),
    TOP_PRODUCTS("Productos Más Vendidos"),
    CUSTOMER_FREQUENCY("Frecuencia de Clientes"),
    DASHBOARD_SUMMARY("Resumen del Dashboard");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
