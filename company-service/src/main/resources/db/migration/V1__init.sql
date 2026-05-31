-- PostGIS 확장 활성화 (public 스키마에 설치 권장)
CREATE EXTENSION IF NOT EXISTS postgis SCHEMA public;

-- company-db 스키마 생성 및 검색 경로 설정
CREATE SCHEMA IF NOT EXISTS "company-db";
-- geometry 타입을 찾기 위해 public을 검색 경로에 포함해야 함
SET search_path TO "company-db", public;

-- p_product_categories (상품 분류)
CREATE TABLE p_product_categories (
    category_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    depth INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(36)
);

-- p_companies (업체)
CREATE TABLE p_companies (
    company_id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    company_type VARCHAR(30) NOT NULL, -- PRODUCER / RECEIVER
    phone VARCHAR(20),
    description TEXT,
    business_number VARCHAR(20) NOT NULL,
    hub_id UUID NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address TEXT,
    logo_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(36)
);

-- p_products (상품)
CREATE TABLE p_products (
    product_id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES p_companies(company_id),
    category_id UUID REFERENCES p_product_categories(category_id),
    name VARCHAR(500) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'ON_SALE', -- ON_SALE / SOLD_OUT
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(36)
);

-- p_product_options (상품 옵션 — SKU 단위)
CREATE TABLE p_product_options (
    product_option_id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES p_products(product_id),
    options_name VARCHAR(255),
    extra_price NUMERIC(12,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'ON_SALE',
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(36)
);

-- p_delivery_addresses (배송지 관리)
CREATE TABLE p_delivery_addresses (
    address_id VARCHAR(36) PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES p_companies(company_id),
    address_name VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    address_detail VARCHAR(255),
    postal_code VARCHAR(10),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(36)
);

-- 인덱스 추가
CREATE INDEX idx_p_companies_hub_id ON p_companies(hub_id);
CREATE INDEX idx_p_products_company_id_status ON p_products(company_id, status);
CREATE INDEX idx_p_companies_deleted_at ON p_companies(deleted_at);
CREATE INDEX idx_p_products_deleted_at ON p_products(deleted_at);
CREATE INDEX idx_p_product_options_deleted_at ON p_product_options(deleted_at);
CREATE INDEX idx_p_delivery_addresses_company_id ON p_delivery_addresses(company_id);
