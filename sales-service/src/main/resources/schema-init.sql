-- Create invoice number sequence if it does not exist
-- This is executed manually or via Flyway/Liquibase migration
CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START 1000 INCREMENT 1;

-- Audit log table for tracking critical operations
CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    details TEXT,
    user_id UUID,
    user_name VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);
