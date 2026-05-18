package com.sparta.delivhub.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON에서 아예 제외함
public class ErrorResponse {
    private int status;
    private String message;
    private List<FieldErrorDetail> errors; // 검증 에러가 있을 때만 나감

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private String field;
        private String message;
    }
}