package com.sparta.companyservice.company.application.service;

import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyDto createCompany(CompanyCreateCommand command) {
        // Application 레이어에서 String -> Domain Enum 변환 (Mapping)
        CompanyTypeEnum type = CompanyTypeEnum.valueOf(command.getCompanyType());

        Company company = Company.builder()
                .companyName(command.getCompanyName())
                .companyType(type)
                .phone(command.getPhone())
                .description(command.getDescription())
                .businessNumber(command.getBusinessNumber())
                .hubId(command.getHubId())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .address(command.getAddress())
                .logoUrl(command.getLogoUrl())
                .build();

        Company savedCompany = companyRepository.save(company);
        return CompanyDto.from(savedCompany);
    }
}
