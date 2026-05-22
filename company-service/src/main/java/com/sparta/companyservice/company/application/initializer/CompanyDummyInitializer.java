package com.sparta.companyservice.company.application.initializer;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyDummyInitializer implements ApplicationRunner {

    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (companyRepository.count() == 0) {
            log.info("🎯 [CompanyService] 로컬 개발용 초기 업체 데이터 주입 시작");

            saveCompany(
                "스파르타 생산 본부", 
                CompanyTypeEnum.PRODUCER, 
                "123-45-67890", 
                126.9780, 37.5665,
                "02-123-4567",
                "전국 물류를 담당하는 메인 생산 거점",
                "서울특별시 중구 세종대로 110",
                "https://cdn.example.com/logos/sparta-prod.png"
            );

            saveCompany(
                "강남 물류 수령 센터", 
                CompanyTypeEnum.RECEIVER, 
                "987-65-43210", 
                127.0276, 37.4979,
                "02-555-1234",
                "강남권 주요 배송 거점 및 수령 센터",
                "서울특별시 강남구 강남대로 390",
                "https://cdn.example.com/logos/gangnam-center.png"
            );

            saveCompany(
                "판교 테크 제조 공장", 
                CompanyTypeEnum.PRODUCER, 
                "111-22-33333", 
                127.1086, 37.4012,
                "031-789-0000",
                "첨단 IT 기기 및 부품 제조 전문 공장",
                "경기도 성남시 분당구 판교역로 166",
                "https://cdn.example.com/logos/pangyo-tech.png"
            );

            log.info("🎯 [CompanyService] 초기 업체 데이터 주입 완료 (3개)");
        }
    }

    private void saveCompany(String name, CompanyTypeEnum type, String bizNumber, double lon, double lat,
                            String phone, String description, String address, String logoUrl) {
        Company company = Company.builder()
                .companyName(name)
                .companyType(type)
                .businessNumber(bizNumber)
                .hubId(UUID.randomUUID()) // 실제 연동 전에는 랜덤 UUID
                .latitude(lat)
                .longitude(lon)
                .phone(phone)
                .description(description)
                .address(address)
                .logoUrl(logoUrl)
                .build();
        
        companyRepository.save(company);
    }
}
