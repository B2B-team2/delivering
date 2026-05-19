package com.sparta.companyservice.company.application.service;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .companyType(request.getCompanyType())
                .phone(request.getPhone())
                .description(request.getDescription())
                .businessNumber(request.getBusinessNumber())
                .hubId(request.getHubId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .logoUrl(request.getLogoUrl())
                .build();

        Company savedCompany = companyRepository.save(company);
        return CompanyResponse.from(savedCompany);
    }
}
