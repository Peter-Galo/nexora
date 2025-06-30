-- SQL Script to populate inventory tables with dummy data
-- Tables: warehouses, products, stocks

-- Clear existing data (if needed)
-- DELETE FROM stocks;
-- DELETE FROM products;
-- DELETE FROM warehouses;

-- Populate warehouses table
INSERT INTO warehouses (code, name, description, address, city, state_province, postal_code, country, created_at, updated_at, is_active)
VALUES
('WH001', 'Main Warehouse', 'Primary storage facility', '123 Storage Ave', 'Warehouse City', 'Storage State', '12345', 'United States', NOW(), NOW(), true),
('WH002', 'East Coast Distribution', 'East coast distribution center', '456 East Blvd', 'New York', 'NY', '10001', 'United States', NOW(), NOW(), true),
('WH003', 'West Coast Distribution', 'West coast distribution center', '789 West St', 'Los Angeles', 'CA', '90001', 'United States', NOW(), NOW(), true),
('WH004', 'European Hub', 'European distribution center', '10 Euro Lane', 'Berlin', 'Berlin', '10115', 'Germany', NOW(), NOW(), true),
('WH005', 'Asian Hub', 'Asian distribution center', '15 Asia Road', 'Tokyo', 'Tokyo', '100-0001', 'Japan', NOW(), NOW(), true);

-- Populate products table
INSERT INTO products (code, name, description, price, created_at, updated_at, is_active, category, brand, sku)
VALUES
-- Electronics
('P001', 'Smartphone X', 'Latest smartphone with advanced features', 999.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'SM-X-001'),
('P002', 'Laptop Pro', 'Professional laptop for developers', 1499.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'LP-PRO-002'),
('P003', 'Wireless Headphones', 'Noise-cancelling wireless headphones', 199.99, NOW(), NOW(), true, 'Electronics', 'AudioTech', 'WH-NC-003'),
('P004', 'Smart Watch', 'Fitness and health tracking smartwatch', 249.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'SW-FIT-004'),
('P005', 'Tablet Ultra', 'Ultra-thin tablet with high resolution display', 699.99, NOW(), NOW(), true, 'Electronics', 'TechBrand', 'TU-HR-005'),

-- Home Appliances
('P006', 'Smart Refrigerator', 'Internet-connected refrigerator with touch screen', 2499.99, NOW(), NOW(), true, 'Home Appliances', 'HomeTech', 'SR-TS-006'),
('P007', 'Robot Vacuum', 'Automated vacuum cleaner with smart mapping', 399.99, NOW(), NOW(), true, 'Home Appliances', 'CleanTech', 'RV-SM-007'),
('P008', 'Air Purifier', 'HEPA filter air purifier for large rooms', 299.99, NOW(), NOW(), true, 'Home Appliances', 'CleanTech', 'AP-LR-008'),
('P009', 'Coffee Maker', 'Programmable coffee maker with built-in grinder', 149.99, NOW(), NOW(), true, 'Home Appliances', 'KitchenPro', 'CM-BG-009'),
('P010', 'Blender Pro', 'High-powered blender for smoothies and more', 129.99, NOW(), NOW(), true, 'Home Appliances', 'KitchenPro', 'BP-HP-010'),

-- Furniture
('P011', 'Ergonomic Office Chair', 'Adjustable office chair with lumbar support', 249.99, NOW(), NOW(), true, 'Furniture', 'ComfortPlus', 'EOC-LS-011'),
('P012', 'Standing Desk', 'Adjustable height standing desk', 399.99, NOW(), NOW(), true, 'Furniture', 'OfficePro', 'SD-AH-012'),
('P013', 'Bookshelf', 'Modern 5-shelf bookcase', 129.99, NOW(), NOW(), true, 'Furniture', 'HomeStyle', 'BS-5S-013'),
('P014', 'Sofa Set', 'Three-piece living room sofa set', 899.99, NOW(), NOW(), true, 'Furniture', 'ComfortPlus', 'SS-3P-014'),
('P015', 'Bed Frame', 'Queen size platform bed frame', 349.99, NOW(), NOW(), true, 'Furniture', 'SleepWell', 'BF-QS-015'),

-- Clothing
('P016', 'Winter Jacket', 'Insulated winter jacket for extreme cold', 199.99, NOW(), NOW(), true, 'Clothing', 'OutdoorLife', 'WJ-EC-016'),
('P017', 'Running Shoes', 'Lightweight running shoes with cushioned sole', 129.99, NOW(), NOW(), true, 'Clothing', 'SportsFit', 'RS-LC-017'),
('P018', 'Dress Shirt', 'Wrinkle-free cotton dress shirt', 59.99, NOW(), NOW(), true, 'Clothing', 'FormalWear', 'DS-WF-018'),
('P019', 'Jeans', 'Classic fit denim jeans', 79.99, NOW(), NOW(), true, 'Clothing', 'CasualStyle', 'JN-CF-019'),
('P020', 'Sunglasses', 'Polarized UV protection sunglasses', 89.99, NOW(), NOW(), true, 'Accessories', 'VisionPro', 'SG-PUV-020'),

-- Inactive Products
('P021', 'Discontinued Phone', 'Previous generation smartphone', 499.99, NOW(), NOW(), false, 'Electronics', 'TechBrand', 'DP-PG-021'),
('P022', 'Outdated Tablet', 'Last year model tablet', 299.99, NOW(), NOW(), false, 'Electronics', 'TechBrand', 'OT-LY-022'),
('P023', 'Seasonal Decor', 'Holiday decorations', 49.99, NOW(), NOW(), false, 'Home Decor', 'SeasonalJoy', 'SD-HD-023');

-- Populate stocks table
INSERT INTO stocks (product_id, warehouse_id, quantity, min_stock_level, max_stock_level, last_restock_date, created_at, updated_at)
VALUES
-- Main Warehouse (WH001) stocks
(1, 1, 100, 20, 200, NOW(), NOW(), NOW()),  -- Smartphone X
(2, 1, 50, 10, 100, NOW(), NOW(), NOW()),   -- Laptop Pro
(3, 1, 200, 30, 300, NOW(), NOW(), NOW()),  -- Wireless Headphones
(4, 1, 150, 25, 250, NOW(), NOW(), NOW()),  -- Smart Watch
(5, 1, 75, 15, 150, NOW(), NOW(), NOW()),   -- Tablet Ultra
(6, 1, 30, 5, 50, NOW(), NOW(), NOW()),     -- Smart Refrigerator
(7, 1, 80, 20, 150, NOW(), NOW(), NOW()),   -- Robot Vacuum
(8, 1, 120, 25, 200, NOW(), NOW(), NOW()),  -- Air Purifier
(9, 1, 100, 20, 150, NOW(), NOW(), NOW()),  -- Coffee Maker
(10, 1, 90, 20, 150, NOW(), NOW(), NOW()),  -- Blender Pro

-- East Coast Distribution (WH002) stocks
(1, 2, 75, 15, 150, NOW(), NOW(), NOW()),   -- Smartphone X
(2, 2, 40, 10, 80, NOW(), NOW(), NOW()),    -- Laptop Pro
(3, 2, 150, 25, 250, NOW(), NOW(), NOW()),  -- Wireless Headphones
(4, 2, 100, 20, 200, NOW(), NOW(), NOW()),  -- Smart Watch
(5, 2, 50, 10, 100, NOW(), NOW(), NOW()),   -- Tablet Ultra
(11, 2, 60, 15, 100, NOW(), NOW(), NOW()),  -- Ergonomic Office Chair
(12, 2, 40, 10, 80, NOW(), NOW(), NOW()),   -- Standing Desk
(13, 2, 70, 15, 120, NOW(), NOW(), NOW()),  -- Bookshelf
(14, 2, 30, 5, 50, NOW(), NOW(), NOW()),    -- Sofa Set
(15, 2, 45, 10, 90, NOW(), NOW(), NOW()),   -- Bed Frame

-- West Coast Distribution (WH003) stocks
(1, 3, 80, 15, 150, NOW(), NOW(), NOW()),   -- Smartphone X
(2, 3, 45, 10, 90, NOW(), NOW(), NOW()),    -- Laptop Pro
(3, 3, 160, 30, 250, NOW(), NOW(), NOW()),  -- Wireless Headphones
(4, 3, 110, 20, 200, NOW(), NOW(), NOW()),  -- Smart Watch
(5, 3, 55, 10, 100, NOW(), NOW(), NOW()),   -- Tablet Ultra
(16, 3, 120, 25, 200, NOW(), NOW(), NOW()), -- Winter Jacket
(17, 3, 150, 30, 250, NOW(), NOW(), NOW()), -- Running Shoes
(18, 3, 200, 40, 300, NOW(), NOW(), NOW()), -- Dress Shirt
(19, 3, 180, 35, 280, NOW(), NOW(), NOW()), -- Jeans
(20, 3, 100, 20, 150, NOW(), NOW(), NOW()), -- Sunglasses

-- European Hub (WH004) stocks
(1, 4, 60, 10, 120, NOW(), NOW(), NOW()),   -- Smartphone X
(2, 4, 30, 5, 60, NOW(), NOW(), NOW()),     -- Laptop Pro
(3, 4, 120, 25, 200, NOW(), NOW(), NOW()),  -- Wireless Headphones
(4, 4, 80, 15, 150, NOW(), NOW(), NOW()),   -- Smart Watch
(5, 4, 40, 10, 80, NOW(), NOW(), NOW()),    -- Tablet Ultra
(16, 4, 100, 20, 180, NOW(), NOW(), NOW()), -- Winter Jacket
(17, 4, 130, 25, 220, NOW(), NOW(), NOW()), -- Running Shoes
(18, 4, 180, 35, 280, NOW(), NOW(), NOW()), -- Dress Shirt
(19, 4, 160, 30, 250, NOW(), NOW(), NOW()), -- Jeans
(20, 4, 90, 20, 140, NOW(), NOW(), NOW()),  -- Sunglasses

-- Asian Hub (WH005) stocks
(1, 5, 70, 15, 140, NOW(), NOW(), NOW()),   -- Smartphone X
(2, 5, 35, 5, 70, NOW(), NOW(), NOW()),     -- Laptop Pro
(3, 5, 140, 25, 220, NOW(), NOW(), NOW()),  -- Wireless Headphones
(4, 5, 90, 20, 170, NOW(), NOW(), NOW()),   -- Smart Watch
(5, 5, 45, 10, 90, NOW(), NOW(), NOW()),    -- Tablet Ultra
(6, 5, 20, 5, 40, NOW(), NOW(), NOW()),     -- Smart Refrigerator
(7, 5, 60, 15, 120, NOW(), NOW(), NOW()),   -- Robot Vacuum
(8, 5, 100, 20, 180, NOW(), NOW(), NOW()),  -- Air Purifier
(9, 5, 80, 15, 140, NOW(), NOW(), NOW()),   -- Coffee Maker
(10, 5, 70, 15, 130, NOW(), NOW(), NOW()),  -- Blender Pro

-- Low stock items (for testing low stock alerts)
(11, 1, 5, 10, 100, NOW(), NOW(), NOW()),   -- Ergonomic Office Chair (below min_stock_level)
(12, 3, 8, 10, 80, NOW(), NOW(), NOW()),    -- Standing Desk (below min_stock_level)
(13, 5, 12, 15, 120, NOW(), NOW(), NOW()),  -- Bookshelf (below min_stock_level)

-- Inactive products with some stock
(21, 1, 10, 0, 20, NOW(), NOW(), NOW()),    -- Discontinued Phone
(22, 1, 15, 0, 30, NOW(), NOW(), NOW()),    -- Outdated Tablet
(23, 1, 25, 0, 50, NOW(), NOW(), NOW());    -- Seasonal Decor