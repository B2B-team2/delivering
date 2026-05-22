package com.sparta.companyservice.product.application.port;

import java.util.UUID;

public interface CompanyQueryPort {
    boolean existsCompanyById(UUID companyId);
}
