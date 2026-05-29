-- =========================================
-- AxisERP - Report Service Database Script
-- =========================================
-- Execute this script in your Neon database console (REPORT_DB).

DROP TABLE IF EXISTS export_history CASCADE;

-- =========================================
-- Export History (audit of all report exports)
-- =========================================
CREATE TABLE export_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(50) NOT NULL
        CHECK (report_type IN ('VENTAS', 'INVENTARIO', 'TOP_PRODUCTOS', 'CLIENTES_FRECUENTES', 'DASHBOARD')),
    export_format VARCHAR(10) NOT NULL
        CHECK (export_format IN ('PDF', 'EXCEL', 'CSV')),
    filters JSONB,
    exported_by UUID NOT NULL,
    exported_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Indexes
-- =========================================
CREATE INDEX idx_export_history_exported_by ON export_history(exported_by);
CREATE INDEX idx_export_history_report_type ON export_history(report_type);
CREATE INDEX idx_export_history_exported_at ON export_history(exported_at);
