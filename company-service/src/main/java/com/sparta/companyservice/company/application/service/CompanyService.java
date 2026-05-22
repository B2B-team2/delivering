package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyUpdateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        return companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())
                .map(existingCompany -> {
                    if (existingCompany.getDeletedAt() == null) {
                        throw new BusinessException(CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);
                    }
                    // 삭제된 상태라면 복구 및 정보 업데이트
                    existingCompany.restore();
                    existingCompany.update(
                            command.getCompanyName(),
                            type,
                            command.getPhone(),
                            command.getDescription(),
                            command.getBusinessNumber(),
                            command.getHubId(),
                            command.getLatitude(),
                            command.getLongitude(),
                            command.getAddress(),
                            command.getLogoUrl()
                    );
                    return CompanyDto.from(existingCompany);
                })
                .orElseGet(() -> {
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

                    try {
                        Company savedCompany = companyRepository.save(company);
                        return CompanyDto.from(savedCompany);
                    } catch (DataIntegrityViolationException e) {
                        throw new BusinessException(CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);
                    }
                });
    }

    @Transactional
    public CompanyDto updateCompany(UUID companyId, CompanyUpdateCommand command) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

        CompanyTypeEnum type;
        try {
            type = CompanyTypeEnum.valueOf(command.getCompanyType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CompanyErrorCode.INVALID_COMPANY_TYPE);
        }

        if (!company.getBusinessNumber().equals(command.getBusinessNumber())) {
            companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())
                    .ifPresent(existing -> {
                        throw new BusinessException(CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);
                    });
        }

        company.update(
                command.getCompanyName(),
                type,
                command.getPhone(),
                command.getDescription(),
                command.getBusinessNumber(),
                command.getHubId(),
                command.getLatitude(),
                command.getLongitude(),
                command.getAddress(),
                command.getLogoUrl()
        );

        return CompanyDto.from(company);
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

    @Transactional
    public CompanyDto deleteCompany(UUID companyId, String username) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

        company.softDelete(username);
        return CompanyDto.from(company);
    }

    public boolean existsCompany(UUID companyId) {
        return companyRepository.findById(companyId).isPresent();
    }
}
