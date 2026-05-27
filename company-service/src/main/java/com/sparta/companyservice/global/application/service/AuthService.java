package com.sparta.companyservice.global.application.service;

import com.sparta.companyservice.company.infrastructure.repository.CompanyDeliveryAddressJpaRepository;
import com.sparta.common.security.CustomUserDetails;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.infrastructure.repository.ProductJpaRepository;
import com.sparta.companyservice.product.infrastructure.repository.ProductOptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authService")
@RequiredArgsConstructor
public class AuthService {

    private final ProductJpaRepository productJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final CompanyDeliveryAddressJpaRepository addressJpaRepository;

    public boolean isCompanyOwner(UUID companyId) {
        CustomUserDetails user = getCurrentUser();
        if (user == null || user.getCompanyId() == null || companyId == null) return false;
        return user.getCompanyId().equals(companyId.toString());
    }

    public boolean isProductOwner(UUID productId) {
        CustomUserDetails user = getCurrentUser();
        if (user == null || user.getCompanyId() == null || productId == null) return false;

        return productJpaRepository.findByProductIdAndDeletedAtIsNull(productId)
                .map(product -> product.getCompanyId().toString().equals(user.getCompanyId()))
                .orElse(false);
    }

    public boolean isProductOwnerByOptionId(UUID productOptionId) {
        CustomUserDetails user = getCurrentUser();
        if (user == null || user.getCompanyId() == null || productOptionId == null) return false;

        return productOptionJpaRepository.findById(productOptionId)
                .map(option -> option.getProduct().getCompanyId().toString().equals(user.getCompanyId()))
                .orElse(false);
    }

    public boolean isAddressOwner(UUID addressId) {
        CustomUserDetails user = getCurrentUser();
        if (user == null || user.getCompanyId() == null || addressId == null) return false;

        return addressJpaRepository.findById(addressId)
                .map(address -> address.getCompanyId().toString().equals(user.getCompanyId()))
                .orElse(false);
    }

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
