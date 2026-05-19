package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreateRequest {

    @NotBlank(message = "업체 이름은 필수 입력 항목입니다.")
    private String companyName;

    @NotNull(message = "업체 타입은 'PRODUCER' 또는 'RECEIVER' 중 하나여야 합니다.")
    private CompanyTypeEnum companyType;

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
}
