package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyAddressUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyAddressUpdateRequest {

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
    private Boolean isDefault;

    public CompanyAddressUpdateCommand toCommand() {
        return CompanyAddressUpdateCommand.builder()
                .addressName(this.addressName)
                .recipientName(this.recipientName)
                .phone(this.phone)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .isDefault(this.isDefault)
                .build();
    }
}
