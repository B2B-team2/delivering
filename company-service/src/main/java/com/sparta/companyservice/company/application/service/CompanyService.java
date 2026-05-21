package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyDto createCompany(CompanyCreateCommand command) {
        CompanyTypeEnum type;
        try {
            type = CompanyTypeEnum.valueOf(command.getCompanyType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CompanyErrorCode.INVALID_COMPANY_TYPE);
        }

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

    @Transactional(readOnly = true)
    public Page<CompanyDto> getCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(CompanyDto::from);
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));
        return CompanyDto.from(company);
    }
}
