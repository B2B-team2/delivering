-- Flyway V3 Migration: Create Product and Delivery Tables
-- Based on plan.md definitions

SET search_path TO "company-db", public;

-- 1. 상품 분류 (p_product_categories)
CREATE TABLE p_product_categories (
    category_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    depth INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- 2. 상품 (p_products)
CREATE TABLE p_products (
    product_id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES p_companies(company_id),
    category_id UUID REFERENCES p_product_categories(category_id),
    name VARCHAR(500) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'ON_SALE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- 3. 상품 옵션 (p_product_options)
CREATE TABLE p_product_options (
    product_option_id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES p_products(product_id),
    options_name VARCHAR(255),
    extra_price NUMERIC(12,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'ON_SALE',
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- 4. 배송지 관리 (p_delivery_addresses)
CREATE TABLE p_delivery_addresses (
    address_id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES p_companies(company_id),
    address_name VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    address_detail VARCHAR(255),
    postal_code VARCHAR(10),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100)
);

-- 인덱스 추가
CREATE INDEX idx_products_company_id ON p_products(company_id);
CREATE INDEX idx_products_category_id ON p_products(category_id);
CREATE INDEX idx_product_options_product_id ON p_product_options(product_id);
CREATE INDEX idx_delivery_addresses_company_id ON p_delivery_addresses(company_id);
