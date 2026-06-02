-- ============================================================================
-- Initialize all databases for AxisERP microservices (LOCAL DEVELOPMENT)
-- Run this script when starting PostgreSQL in Docker
-- ============================================================================

-- Create databases for each microservice
CREATE DATABASE axiserp_auth;
CREATE DATABASE axiserp_catalog;
CREATE DATABASE axiserp_inventory;
CREATE DATABASE axiserp_sales;
CREATE DATABASE axiserp_purchase;
CREATE DATABASE axiserp_report;

-- Grant privileges to postgres user
GRANT ALL PRIVILEGES ON DATABASE axiserp_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE axiserp_catalog TO postgres;
GRANT ALL PRIVILEGES ON DATABASE axiserp_inventory TO postgres;
GRANT ALL PRIVILEGES ON DATABASE axiserp_sales TO postgres;
GRANT ALL PRIVILEGES ON DATABASE axiserp_purchase TO postgres;
GRANT ALL PRIVILEGES ON DATABASE axiserp_report TO postgres;
