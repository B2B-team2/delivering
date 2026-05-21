-- p_companies 테이블의 business_number 컬럼에 UNIQUE 제약 조건 추가
ALTER TABLE "company-db".p_companies ADD CONSTRAINT uq_companies_business_number UNIQUE (business_number);
