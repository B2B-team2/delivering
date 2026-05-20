package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyCreateRequest {

    @NotBlank(message = "업체명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "업체 타입은 필수입니다.")
    private String companyType; // Domain Enum을 직접 사용하지 않음

    private String phone;

    private String description;

    @NotBlank(message = "사업자 등록 번호는 필수 입력 항목입니다.")
    private String businessNumber;

    @NotNull(message = "허브 ID는 필수 입력 항목입니다.")
    private UUID hubId;

    @NotNull(message = "위도는 필수 입력 항목입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수 입력 항목입니다.")
    private Double longitude;

    private String address;

    private String logoUrl;

    public CompanyCreateCommand toCommand() {
        return CompanyCreateCommand.builder()
                .companyName(companyName)
                .companyType(companyType)
                .phone(phone)
                .description(description)
                .businessNumber(businessNumber)
                .hubId(hubId)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .logoUrl(logoUrl)
                .build();
    }
}
