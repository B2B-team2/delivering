package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyDeliveryAddressCreateCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDeliveryAddressCreateRequest {

    @NotBlank(message = "배송지 이름은 필수입니다.")
    private String addressName;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String recipientName;

    @NotBlank(message = "연락처는 필수입니다.")
    private String phone;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    private String addressDetail;

    private String postalCode;

    private Boolean isDefault;

    public CompanyDeliveryAddressCreateCommand toCommand(UUID companyId) {
        return CompanyDeliveryAddressCreateCommand.builder()
                .companyId(companyId)
                .addressName(addressName)
                .recipientName(recipientName)
                .phone(phone)
                .address(address)
                .addressDetail(addressDetail)
                .postalCode(postalCode)
                .isDefault(isDefault != null ? isDefault : false)
                .build();
    }
}
