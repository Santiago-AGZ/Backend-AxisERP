-- =========================================
-- AxisERP - Catalog Service Database Script
-- =========================================
-- Execute this script in your Neon database console (CATALOG_DB).

DROP TABLE IF EXISTS product_barcodes CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- =========================================
-- Categories (supports subcategories via parent_id)
-- =========================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
        CHECK (status IN ('ACTIVA', 'INACTIVA', 'ELIMINADA')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Products
-- =========================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    category_id UUID NOT NULL REFERENCES categories(id),
    purchase_price DECIMAL(10, 2) NOT NULL CHECK (purchase_price >= 0),
    sale_price DECIMAL(10, 2) NOT NULL CHECK (sale_price > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CHECK (status IN ('ACTIVO', 'INACTIVO', 'ELIMINADO')),
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Product Barcodes (multi-barcode support; one primary)
-- =========================================
CREATE TABLE product_barcodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    barcode VARCHAR(50) NOT NULL UNIQUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Indexes
-- =========================================
CREATE INDEX idx_categories_status ON categories(status);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_codigo ON products(codigo);
CREATE INDEX idx_product_barcodes_product_id ON product_barcodes(product_id);
