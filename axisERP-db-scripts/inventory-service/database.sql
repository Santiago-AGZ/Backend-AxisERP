-- =========================================
-- AxisERP - Inventory Service Database Script
-- =========================================
-- Execute this script in your Neon database console (INVENTORY_DB).

DROP TABLE IF EXISTS inventory_movements CASCADE;
DROP TABLE IF EXISTS inventories CASCADE;

-- =========================================
-- Inventories (one record per product)
-- Uses optimistic locking via version column
-- =========================================
CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE,
    current_stock INT NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    min_stock INT NOT NULL DEFAULT 0 CHECK (min_stock >= 0),
    max_stock INT NOT NULL DEFAULT 0 CHECK (max_stock >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Inventory Movements (immutable — never update rows)
-- =========================================
CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL REFERENCES inventories(id),
    product_id UUID NOT NULL,
    movement_type VARCHAR(30) NOT NULL
        CHECK (movement_type IN (
            'INVENTARIO_INICIAL', 'ENTRADA', 'SALIDA',
            'AJUSTE_POSITIVO', 'AJUSTE_NEGATIVO', 'DEVOLUCION', 'ANULACION'
        )),
    quantity INT NOT NULL CHECK (quantity > 0),
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL CHECK (new_stock >= 0),
    reference_type VARCHAR(50),
    reference_id UUID,
    justification TEXT,
    notes TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Indexes
-- =========================================
CREATE INDEX idx_inventories_product_id ON inventories(product_id);
CREATE INDEX idx_movements_inventory_id ON inventory_movements(inventory_id);
CREATE INDEX idx_movements_product_id ON inventory_movements(product_id);
CREATE INDEX idx_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_movements_reference ON inventory_movements(reference_type, reference_id);
CREATE INDEX idx_movements_created_at ON inventory_movements(created_at);
