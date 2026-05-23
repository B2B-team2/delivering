package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyAddressCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
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
@Transactional(readOnly = true)
public class CompanyAddressService {

    private final CompanyDeliveryAddressRepository companyDeliveryAddressRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyAddressDto registerAddress(UUID companyId, CompanyAddressCreateCommand command) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND);
        }

        if (command.getIsDefault()) {
            companyDeliveryAddressRepository.updateAllIsDefaultFalseByCompanyId(companyId);
        }

        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .companyId(companyId)
                .addressName(command.getAddressName())
                .recipientName(command.getRecipientName())
                .phone(command.getPhone())
                .address(command.getAddress())
                .addressDetail(command.getAddressDetail())
                .postalCode(command.getPostalCode())
                .isDefault(command.getIsDefault())
                .build();

        return CompanyAddressDto.from(companyDeliveryAddressRepository.save(address));
    }

    public Page<CompanyAddressDto> getAddresses(UUID companyId, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND);
        }
        return companyDeliveryAddressRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable)
                .map(CompanyAddressDto::from);
    }
}
