package com.sparta.companyservice.product.infrastructure.adapter;

import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.product.application.port.CompanyQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyQueryAdapter implements CompanyQueryPort {

    private final CompanyService companyService;

    @Override
    public boolean existsCompanyById(UUID companyId) {
        return companyService.existsCompany(companyId);
    }
}
