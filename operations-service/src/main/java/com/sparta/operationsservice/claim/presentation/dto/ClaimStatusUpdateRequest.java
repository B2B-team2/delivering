package com.sparta.operationsservice.claim.presentation.dto;

import com.sparta.operationsservice.claim.application.dto.ClaimStatusUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClaimStatusUpdateRequest {

    @NotBlank(message = "상태값은 필수입니다.")
    private String status;

    public ClaimStatusUpdateCommand toCommand() {
        return ClaimStatusUpdateCommand.builder()
                .status(this.status)
                .build();
    }
}
