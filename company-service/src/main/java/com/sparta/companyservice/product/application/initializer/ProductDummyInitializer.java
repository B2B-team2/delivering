package com.sparta.companyservice.product.application.initializer;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDummyInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductOptionRepository optionRepository;
    private final CompanyRepository companyRepository;
    private final CompanyDeliveryAddressRepository deliveryAddressRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() == 0) {
            log.info("🎯 [CompanyService] 초기 상품 카테고리 데이터 주입 시작");
            ProductCategory electronics = saveCategory("전자제품");
            ProductCategory clothing = saveCategory("의류");
            ProductCategory food = saveCategory("식품");

            List<Company> companies = companyRepository.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (companies.isEmpty()) {
                log.warn("⚠️ [CompanyService] 등록된 업체가 없어 상품 및 배송지 주입을 스킵합니다.");
                return;
            }

            Company producer = companies.get(0); // 스파르타 생산 본부 가정
            Company techFactory = companies.size() > 2 ? companies.get(2) : producer;

            log.info("🎯 [CompanyService] 초기 상품 데이터 주입 시작");
            Product macbook = saveProduct(producer.getCompanyId(), electronics.getCategoryId(), "맥북 프로 M3", 3500000, "최신형 M3 칩 탑재 맥북", "https://example.com/macbook.png");
            Product galaxy = saveProduct(producer.getCompanyId(), electronics.getCategoryId(), "갤럭시 S24", 1200000, "최첨단 AI 스마트폰", "https://example.com/galaxy.png");
            Product keyboard = saveProduct(techFactory.getCompanyId(), electronics.getCategoryId(), "기계식 키보드", 150000, "조용한 타건감의 저소음 적축", "https://example.com/keyboard.png");
            Product monitor = saveProduct(techFactory.getCompanyId(), electronics.getCategoryId(), "4K 모니터", 600000, "선명한 화질의 32인치 모니터", "https://example.com/monitor.png");
            Product cable = saveProduct(producer.getCompanyId(), electronics.getCategoryId(), "고속 충전 케이블", 15000, "튼튼한 내구성의 C-type 케이블", "https://example.com/cable.png");

            log.info("🎯 [CompanyService] 초기 상품 옵션 데이터 주입 시작");
            saveOption(macbook, "RAM 16GB", 0, 1);
            saveOption(macbook, "RAM 32GB", 300000, 2);
            saveOption(galaxy, "256GB", 0, 1);
            saveOption(galaxy, "512GB", 150000, 2);

            log.info("🎯 [CompanyService] 초기 배송지 데이터 주입 시작");
            if (deliveryAddressRepository.count() == 0) {
                saveCompanyDeliveryAddress(producer, "본사 수령지", "관리자", "02-123-4567", "서울특별시 중구 세종대로 110", "본관 1층", "04524", true);
                saveCompanyDeliveryAddress(techFactory, "제조공장 하차장", "공장장", "031-789-0000", "경기도 성남시 분당구 판교역로 166", "A동 창고", "13486", true);
            }

            log.info("🎯 [CompanyService] 초기 도메인 데이터 주입 완료");
        }
    }

    private ProductCategory saveCategory(String name) {
        ProductCategory category = ProductCategory.builder()
                .name(name)
                .depth(1)
                .build();
        return categoryRepository.save(category);
    }

    private Product saveProduct(java.util.UUID companyId, java.util.UUID categoryId, String name, long price, String desc, String thumb) {
        Product product = Product.builder()
                .companyId(companyId)
                .categoryId(categoryId)
                .name(name)
                .price(BigDecimal.valueOf(price))
                .description(desc)
                .thumbnailUrl(thumb)
                .status(ProductStatusEnum.ON_SALE)
                .build();
        return productRepository.save(product);
    }

    private void saveOption(Product product, String name, long extraPrice, int order) {
        ProductOption option = ProductOption.builder()
                .product(product)
                .optionsName(name)
                .extraPrice(BigDecimal.valueOf(extraPrice))
                .status(ProductStatusEnum.ON_SALE)
                .displayOrder(order)
                .build();
        optionRepository.save(option);
    }

    private void saveCompanyDeliveryAddress(Company company, String name, String recipient, String phone, String address, String detail, String zip, boolean isDefault) {
        CompanyDeliveryAddress deliveryAddress = CompanyDeliveryAddress.builder()
                .companyId(company.getCompanyId())
                .addressName(name)
                .recipientName(recipient)
                .phone(phone)
                .address(address)
                .addressDetail(detail)
                .postalCode(zip)
                .isDefault(isDefault)
                .build();
        deliveryAddressRepository.save(deliveryAddress);
    }
}
