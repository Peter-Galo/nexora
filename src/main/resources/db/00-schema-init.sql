-- Database Schema Initialization Script
-- This script creates all necessary tables before data population

-- Create users table (no dependencies)
CREATE TABLE IF NOT EXISTS public.users
(
    uuid       uuid         NOT NULL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL
    );

ALTER TABLE public.users
    OWNER TO nexora;

-- Create user_roles table (depends on users)
CREATE TABLE IF NOT EXISTS public.user_roles
(
    uuid      uuid         NOT NULL PRIMARY KEY,
    role      VARCHAR(255) NOT NULL
    CONSTRAINT user_roles_role_check
    CHECK ((role)::TEXT = ANY
((ARRAY ['USER'::CHARACTER VARYING, 'ADMIN'::CHARACTER VARYING, 'MANAGER'::CHARACTER VARYING])::TEXT[])),
    user_uuid uuid         NOT NULL
    CONSTRAINT fkb4bms60ebskkrd05297us35x9
    REFERENCES public.users,
    CONSTRAINT ukekn7jt54ck69yo926b2800u3u
    UNIQUE (user_uuid, role)
    );

ALTER TABLE public.user_roles
    OWNER TO nexora;

-- Create products table (no dependencies)
CREATE TABLE IF NOT EXISTS public.products
(
    uuid        uuid           NOT NULL PRIMARY KEY,
    is_active   BOOLEAN,
    brand       VARCHAR(100),
    category    VARCHAR(100),
    code        VARCHAR(50)    NOT NULL UNIQUE,
    created_at  TIMESTAMP(6),
    description VARCHAR(500),
    name        VARCHAR(100)   NOT NULL,
    price       NUMERIC(38, 2) NOT NULL,
    sku         VARCHAR(50),
    updated_at  TIMESTAMP(6)
    );

ALTER TABLE public.products
    OWNER TO nexora;

-- Create warehouses table (no dependencies)
CREATE TABLE IF NOT EXISTS public.warehouses
(
    uuid           uuid         NOT NULL PRIMARY KEY,
    is_active      BOOLEAN,
    address        VARCHAR(200) NOT NULL,
    city           VARCHAR(100) NOT NULL,
    code           VARCHAR(50)  NOT NULL UNIQUE,
    country        VARCHAR(100) NOT NULL,
    created_at     TIMESTAMP(6),
    description    VARCHAR(500),
    name           VARCHAR(100) NOT NULL,
    postal_code    VARCHAR(20),
    state_province VARCHAR(100),
    updated_at     TIMESTAMP(6)
    );

ALTER TABLE public.warehouses
    OWNER TO nexora;

-- Create stocks table (depends on products and warehouses)
CREATE TABLE IF NOT EXISTS public.stocks
(
    uuid              uuid    NOT NULL PRIMARY KEY,
    created_at        TIMESTAMP(6),
    last_restock_date TIMESTAMP(6),
    max_stock_level   INTEGER
    CONSTRAINT stocks_max_stock_level_check
    CHECK (max_stock_level >= 0),
    min_stock_level   INTEGER
    CONSTRAINT stocks_min_stock_level_check
    CHECK (min_stock_level >= 0),
    quantity          INTEGER NOT NULL
    CONSTRAINT stocks_quantity_check
    CHECK (quantity >= 0),
    updated_at        TIMESTAMP(6),
    product_uuid      uuid    NOT NULL
    CONSTRAINT fkf4bwntagwq1fq3y0y5t94f15c
    REFERENCES public.products,
    warehouse_uuid    uuid    NOT NULL
    CONSTRAINT fkfjda89qdrvy6gfwk5s1w67tjc
    REFERENCES public.warehouses,
    CONSTRAINT ukgu9xbibfd4a5dpybm8jvh1492
    UNIQUE (product_uuid, warehouse_uuid)
    );

ALTER TABLE public.stocks
    OWNER TO nexora;

-- Create export_jobs table (references users)
CREATE TABLE IF NOT EXISTS public.export_jobs
(
    uuid          uuid         NOT NULL PRIMARY KEY,
    category      VARCHAR(255) NOT NULL
    CONSTRAINT export_jobs_category_check
    CHECK ((category)::TEXT = ANY
((ARRAY ['PRODUCT'::CHARACTER VARYING, 'STOCK'::CHARACTER VARYING, 'WAREHOUSE'::CHARACTER VARYING])::TEXT[])),
    created_at    TIMESTAMP(6),
    error_message VARCHAR(500),
    export_type   VARCHAR(255) NOT NULL,
    file_url      VARCHAR(255),
    status        VARCHAR(255) NOT NULL
    CONSTRAINT export_jobs_status_check
    CHECK ((status)::TEXT = ANY
((ARRAY ['PENDING'::CHARACTER VARYING, 'PROCESSING'::CHARACTER VARYING, 'COMPLETED'::CHARACTER VARYING, 'FAILED'::CHARACTER VARYING])::TEXT[])),
    updated_at    TIMESTAMP(6),
    user_uuid     uuid         NOT NULL
    );

ALTER TABLE public.export_jobs
    OWNER TO nexora;