-- =========================================
-- AxisERP - Sales Service Database Script
-- =========================================
-- Execute this script in your Neon database console (SALES_DB).

DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS sale_items CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

-- =========================================
-- Customers
-- =========================================
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    document_type VARCHAR(20) NOT NULL DEFAULT 'CC',
    document_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
        CHECK (status IN ('ACTIVO', 'INACTIVO', 'ELIMINADO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Sales
-- State machine: BORRADOR -> PENDIENTE -> CONFIRMADA -> PAGADA
--                CONFIRMADA/PAGADA -> ANULADA (with stock reversal)
-- Uses optimistic locking for concurrency control
-- =========================================
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    sale_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
        CHECK (status IN ('BORRADOR', 'PENDIENTE', 'CONFIRMADA', 'PAGADA', 'ANULADA')),
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    tax DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (tax >= 0),
    total DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    notes TEXT,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Sale Items (one product per line, no duplicates in same sale)
-- =========================================
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    subtotal DECIMAL(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- One product per sale (no duplicates)
CREATE UNIQUE INDEX idx_sale_items_no_dup ON sale_items(sale_id, product_id);

-- =========================================
-- Invoice Number Sequence (sequential, immutable)
-- =========================================
CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START 1 INCREMENT 1;

-- =========================================
-- Invoices (immutable snapshot of confirmed sale)
-- =========================================
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL UNIQUE REFERENCES sales(id),
    invoice_number BIGINT NOT NULL UNIQUE DEFAULT nextval('invoice_number_seq'),
    customer_snapshot JSONB NOT NULL,
    items_snapshot JSONB NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    tax DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total DECIMAL(12, 2) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- Indexes
-- =========================================
CREATE INDEX idx_customers_document ON customers(document_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_sales_number ON sales(sale_number);
CREATE INDEX idx_sales_created_at ON sales(created_at);
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);
CREATE INDEX idx_invoices_sale_id ON invoices(sale_id);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_issued_at ON invoices(issued_at);
