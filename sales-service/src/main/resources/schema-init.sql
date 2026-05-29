-- Create invoice number sequence if it does not exist
-- This is executed manually or via Flyway/Liquibase migration
CREATE SEQUENCE IF NOT EXISTS invoice_number_seq START 1000 INCREMENT 1;
