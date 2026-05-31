package com.sparta.companyservice.global.application.service;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.infrastructure.repository.CompanyDeliveryAddressJpaRepository;
import com.sparta.common.security.CustomUserDetails;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.infrastructure.repository.ProductJpaRepository;
import com.sparta.companyservice.product.infrastructure.repository.ProductOptionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ProductJpaRepository productJpaRepository;
    @Mock
    private ProductOptionJpaRepository productOptionJpaRepository;
    @Mock
    private CompanyDeliveryAddressJpaRepository addressJpaRepository;

    @InjectMocks
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId.toString(),
                "COMPANY_MANAGER",
                companyId.toString(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_COMPANY_MANAGER"))
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("업체 소유권 확인 - 성공")
    void isCompanyOwner_Success() {
        assertTrue(authService.isCompanyOwner(companyId));
    }

    @Test
    @DisplayName("업체 소유권 확인 - 실패 (다른 업체 ID)")
    void isCompanyOwner_Fail() {
        assertFalse(authService.isCompanyOwner(UUID.randomUUID()));
    }

    @Test
    @DisplayName("상품 소유권 확인 - 성공")
    void isProductOwner_Success() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .productId(productId)
                .companyId(companyId)
                .build();

        when(productJpaRepository.findByProductIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product));

        assertTrue(authService.isProductOwner(productId));
    }

    @Test
    @DisplayName("배송지 소유권 확인 - 성공")
    void isAddressOwner_Success() {
        UUID addressId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(addressId)
                .companyId(companyId)
                .build();

        when(addressJpaRepository.findById(addressId)).thenReturn(Optional.of(address));

        assertTrue(authService.isAddressOwner(addressId));
    }
}
