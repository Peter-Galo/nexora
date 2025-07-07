-- Initialize Nexora database schema
-- This script runs when the PostgreSQL container starts for the first time

-- Create the nexora schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS nexora;

-- Set the default schema for this session
SET search_path TO nexora;

-- Grant necessary permissions to the nexora user
GRANT ALL PRIVILEGES ON SCHEMA nexora TO nexora;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA nexora TO nexora;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA nexora TO nexora;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA nexora GRANT ALL ON TABLES TO nexora;
ALTER DEFAULT PRIVILEGES IN SCHEMA nexora GRANT ALL ON SEQUENCES TO nexora;

-- Note: Hibernate will create the actual tables based on JPA entities
-- when the application starts with ddl-auto: update