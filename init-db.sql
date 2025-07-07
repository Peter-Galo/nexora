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
INSERT INTO warehouses (uuid, code, name, description, address, city, state_province, postal_code, country, created_at, updated_at, is_active)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'WH001', 'Main Warehouse', 'Primary storage facility', '123 Storage Ave', 'Warehouse City', 'Storage State', '12345', 'United States', NOW(), NOW(), true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'WH002', 'East Coast Distribution', 'East coast distribution center', '456 East Blvd', 'New York', 'NY', '10001', 'United States', NOW(), NOW(), true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'WH003', 'West Coast Distribution', 'West coast distribution center', '789 West St', 'Los Angeles', 'CA', '90001', 'United States', NOW(), NOW(), true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'WH004', 'European Hub', 'European distribution center', '10 Euro Lane', 'Berlin', 'Berlin', '10115', 'Germany', NOW(), NOW(), true),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'WH005', 'Asian Hub', 'Asian distribution center', '15 Asia Road', 'Tokyo', 'Tokyo', '100-0001', 'Japan', NOW(), NOW(), true);

-- Populate products table
INSERT INTO products (uuid, code, name, description, price, created_at, updated_at, is_active, category, brand, sku)
VALUES
-- Electronics
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'P001', 'Smartphone X', 'Latest smartphone with advanced features', 999.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'SM-X-001'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'P002', 'Laptop Pro', 'Professional laptop for developers', 1499.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'LP-PRO-002'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'P003', 'Wireless Headphones', 'Noise-cancelling wireless headphones', 199.99, NOW(), NOW(), true, 'Electronics', 'AudioTech', 'WH-NC-003'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'P004', 'Smart Watch', 'Fitness and health tracking smartwatch', 249.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'SW-FIT-004'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'P005', 'Tablet Ultra', 'Ultra-thin tablet with high resolution display', 699.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'TU-HR-005'),

-- Home Appliances
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'P006', 'Smart Refrigerator', 'Internet-connected refrigerator with touch screen', 2499.99, NOW(), NOW(), true, 'Home Appliances', 'HomeTech', 'SR-TS-006'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'P007', 'Robot Vacuum', 'Automated vacuum cleaner with smart mapping', 399.99, NOW(), NOW(), true, 'Home Appliances', 'CleanTech', 'RV-SM-007'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'P008', 'Air Purifier', 'HEPA filter air purifier for large rooms', 299.99, NOW(), NOW(), true, 'Home Appliances', 'CleanTech', 'AP-LR-008'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'P009', 'Coffee Maker', 'Programmable coffee maker with built-in grinder', 149.99, NOW(), NOW(), true, 'Home Appliances', 'KitchenPro', 'CM-BG-009'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'P010', 'Blender Pro', 'High-powered blender for smoothies and more', 129.99, NOW(), NOW(), true, 'Home Appliances', 'KitchenPro', 'BP-HP-010'),

-- Furniture
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'P011', 'Ergonomic Office Chair', 'Adjustable office chair with lumbar support', 249.99, NOW(), NOW(), true, 'Furniture', 'ComfortPlus', 'EOC-LS-011'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'P012', 'Standing Desk', 'Adjustable height standing desk', 399.99, NOW(), NOW(), true, 'Furniture', 'OfficePro', 'SD-AH-012'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'P013', 'Bookshelf', 'Modern 5-shelf bookcase', 129.99, NOW(), NOW(), true, 'Furniture', 'HomeStyle', 'BS-5S-013'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'P014', 'Sofa Set', 'Three-piece living room sofa set', 899.99, NOW(), NOW(), true, 'Furniture', 'ComfortPlus', 'SS-3P-014'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'P015', 'Bed Frame', 'Queen size platform bed frame', 349.99, NOW(), NOW(), true, 'Furniture', 'SleepWell', 'BF-QS-015'),

-- Clothing
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'P016', 'Winter Jacket', 'Insulated winter jacket for extreme cold', 199.99, NOW(), NOW(), true, 'Clothing', 'OutdoorLife', 'WJ-EC-016'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'P017', 'Running Shoes', 'Lightweight running shoes with cushioned sole', 129.99, NOW(), NOW(), true, 'Clothing', 'SportsFit', 'RS-LC-017'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'P018', 'Dress Shirt', 'Wrinkle-free cotton dress shirt', 59.99, NOW(), NOW(), true, 'Clothing', 'FormalWear', 'DS-WF-018'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'P019', 'Jeans', 'Classic fit denim jeans', 79.99, NOW(), NOW(), true, 'Clothing', 'CasualStyle', 'JN-CF-019'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'P020', 'Sunglasses', 'Polarized UV protection sunglasses', 89.99, NOW(), NOW(), true, 'Accessories', 'VisionPro', 'SG-PUV-020'),

-- Inactive Products
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'P021', 'Discontinued Phone', 'Previous generation smartphone', 499.99, NOW(), NOW(), false, 'Electronics', 'TechBrand', 'DP-PG-021'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'P022', 'Outdated Tablet', 'Last year model tablet', 299.99, NOW(), NOW(), false, 'Electronics', 'TechBrand', 'OT-LY-022'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'P023', 'Seasonal Decor', 'Holiday decorations', 49.99, NOW(), NOW(), false, 'Home Decor', 'SeasonalJoy', 'SD-HD-023');

-- Populate stocks table
INSERT INTO stocks (uuid, product_uuid, warehouse_uuid, quantity, min_stock_level, max_stock_level, last_restock_date, created_at, updated_at)
VALUES
-- Main Warehouse (WH001) stocks
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 100, 20, 200, NOW(), NOW(), NOW()),  -- Smartphone X
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 50, 10, 100, NOW(), NOW(), NOW()),   -- Laptop Pro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 200, 30, 300, NOW(), NOW(), NOW()),  -- Wireless Headphones
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 150, 25, 250, NOW(), NOW(), NOW()),  -- Smart Watch
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 75, 15, 150, NOW(), NOW(), NOW()),   -- Tablet Ultra
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 30, 5, 50, NOW(), NOW(), NOW()),     -- Smart Refrigerator
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 80, 20, 150, NOW(), NOW(), NOW()),   -- Robot Vacuum
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 120, 25, 200, NOW(), NOW(), NOW()),  -- Air Purifier
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 100, 20, 150, NOW(), NOW(), NOW()),  -- Coffee Maker
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 90, 20, 150, NOW(), NOW(), NOW()),   -- Blender Pro

-- East Coast Distribution (WH002) stocks
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 75, 15, 150, NOW(), NOW(), NOW()),   -- Smartphone X
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 40, 10, 80, NOW(), NOW(), NOW()),    -- Laptop Pro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 150, 25, 250, NOW(), NOW(), NOW()),  -- Wireless Headphones
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 100, 20, 200, NOW(), NOW(), NOW()),  -- Smart Watch
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 50, 10, 100, NOW(), NOW(), NOW()),   -- Tablet Ultra
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 60, 15, 100, NOW(), NOW(), NOW()),  -- Ergonomic Office Chair
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 40, 10, 80, NOW(), NOW(), NOW()),   -- Standing Desk
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 70, 15, 120, NOW(), NOW(), NOW()),  -- Bookshelf
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 30, 5, 50, NOW(), NOW(), NOW()),    -- Sofa Set
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 45, 10, 90, NOW(), NOW(), NOW()),   -- Bed Frame

-- West Coast Distribution (WH003) stocks
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 80, 15, 150, NOW(), NOW(), NOW()),   -- Smartphone X
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 45, 10, 90, NOW(), NOW(), NOW()),    -- Laptop Pro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 160, 30, 250, NOW(), NOW(), NOW()),  -- Wireless Headphones
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 110, 20, 200, NOW(), NOW(), NOW()),  -- Smart Watch
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a35', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 55, 10, 100, NOW(), NOW(), NOW()),   -- Tablet Ultra
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a36', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 120, 25, 200, NOW(), NOW(), NOW()), -- Winter Jacket
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a37', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 150, 30, 250, NOW(), NOW(), NOW()), -- Running Shoes
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a38', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 200, 40, 300, NOW(), NOW(), NOW()), -- Dress Shirt
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a39', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 180, 35, 280, NOW(), NOW(), NOW()), -- Jeans
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a40', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 100, 20, 150, NOW(), NOW(), NOW()), -- Sunglasses

-- European Hub (WH004) stocks
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 60, 10, 120, NOW(), NOW(), NOW()),   -- Smartphone X
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 30, 5, 60, NOW(), NOW(), NOW()),     -- Laptop Pro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 120, 25, 200, NOW(), NOW(), NOW()),  -- Wireless Headphones
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 80, 15, 150, NOW(), NOW(), NOW()),   -- Smart Watch
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 40, 10, 80, NOW(), NOW(), NOW()),    -- Tablet Ultra
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a46', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 100, 20, 180, NOW(), NOW(), NOW()), -- Winter Jacket
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a47', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 130, 25, 220, NOW(), NOW(), NOW()), -- Running Shoes
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a48', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 180, 35, 280, NOW(), NOW(), NOW()), -- Dress Shirt
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a49', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 160, 30, 250, NOW(), NOW(), NOW()), -- Jeans
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a50', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a30', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 90, 20, 140, NOW(), NOW(), NOW()),  -- Sunglasses

-- Asian Hub (WH005) stocks
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a51', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 70, 15, 140, NOW(), NOW(), NOW()),   -- Smartphone X
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a52', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 35, 5, 70, NOW(), NOW(), NOW()),     -- Laptop Pro
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a53', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 140, 25, 220, NOW(), NOW(), NOW()),  -- Wireless Headphones
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a54', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 90, 20, 170, NOW(), NOW(), NOW()),   -- Smart Watch
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 45, 10, 90, NOW(), NOW(), NOW()),    -- Tablet Ultra
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a56', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 20, 5, 40, NOW(), NOW(), NOW()),     -- Smart Refrigerator
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a57', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 60, 15, 120, NOW(), NOW(), NOW()),   -- Robot Vacuum
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a58', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 100, 20, 180, NOW(), NOW(), NOW()),  -- Air Purifier
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a59', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 80, 15, 140, NOW(), NOW(), NOW()),   -- Coffee Maker
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a60', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 70, 15, 130, NOW(), NOW(), NOW()),   -- Blender Pro

-- Low stock items (for testing low stock alerts)
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a61', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 5, 10, 100, NOW(), NOW(), NOW()),   -- Ergonomic Office Chair (below min_stock_level)
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a62', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 8, 10, 80, NOW(), NOW(), NOW()),    -- Standing Desk (below min_stock_level)
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a63', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 12, 15, 120, NOW(), NOW(), NOW()),  -- Bookshelf (below min_stock_level)

-- Inactive products with some stock
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a64', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 10, 0, 20, NOW(), NOW(), NOW()),    -- Discontinued Phone
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a65', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 15, 0, 30, NOW(), NOW(), NOW()),    -- Outdated Tablet
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 25, 0, 50, NOW(), NOW(), NOW());    -- Seasonal Decor
