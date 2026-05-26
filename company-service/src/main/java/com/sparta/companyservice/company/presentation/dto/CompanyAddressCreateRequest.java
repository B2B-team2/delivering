package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyAddressCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyAddressCreateRequest {
    @NotNull(message = "업체 ID는 필수입니다.")
    private UUID companyId;

    @NotBlank(message = "주소 별칭은 필수입니다.")
    private String addressName;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String recipientName;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private String addressDetail;

    private String postalCode;

    @NotNull
    @Builder.Default
    private Boolean isDefault = false;

    public CompanyAddressCreateCommand toCommand() {
        return CompanyAddressCreateCommand.builder()
                .companyId(this.companyId)
                .addressName(this.addressName)
                .recipientName(this.recipientName)
                .phone(this.phone)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .isDefault(this.isDefault != null ? this.isDefault : false)
                .build();
    }
}
