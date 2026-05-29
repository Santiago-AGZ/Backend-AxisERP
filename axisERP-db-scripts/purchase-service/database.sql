-- =========================================
-- AxisERP - Purchase Service Database Script
-- =========================================
-- Execute this script in your Neon database console (PURCHASE_DB).

DROP TABLE IF EXISTS purchase_items CASCADE;
DROP TABLE IF EXISTS purchases CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;

-- =========================================
-- Suppliers
-- =========================================
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CHECK (status IN ('ACTIVO', 'INACTIVO', 'ELIMINADO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Purchases
-- State machine: BORRADOR -> PENDIENTE -> RECIBIDA -> PAGADA
--                BORRADOR/PENDIENTE -> CANCELADA
-- =========================================
CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    purchase_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
        CHECK (status IN ('BORRADOR', 'PENDIENTE', 'RECIBIDA', 'PAGADA', 'CANCELADA')),
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (tax >= 0),
    total DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    notes TEXT,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Purchase Items (one product per line, no duplicates in same purchase)
-- received_quantity tracks partial reception
-- =========================================
CREATE TABLE purchase_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_id UUID NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    received_quantity INT NOT NULL DEFAULT 0 CHECK (received_quantity >= 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- One product per purchase (no duplicates)
CREATE UNIQUE INDEX idx_purchase_items_no_dup ON purchase_items(purchase_id, product_id);

-- =========================================
-- Indexes
-- =========================================
CREATE INDEX idx_suppliers_nit ON suppliers(nit);
CREATE INDEX idx_suppliers_status ON suppliers(status);
CREATE INDEX idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_purchases_number ON purchases(purchase_number);
CREATE INDEX idx_purchase_items_purchase_id ON purchase_items(purchase_id);
CREATE INDEX idx_purchase_items_product_id ON purchase_items(product_id);
