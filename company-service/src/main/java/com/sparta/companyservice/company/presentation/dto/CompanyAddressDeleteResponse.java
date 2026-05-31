package com.sparta.companyservice.company.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAddressDeleteResponse {
    private UUID addressId;
    private LocalDateTime deletedAt;

    public static CompanyAddressDeleteResponse of(UUID addressId, LocalDateTime deletedAt) {
        return CompanyAddressDeleteResponse.builder()
                .addressId(addressId)
                .deletedAt(deletedAt)
                .build();
    }
}
