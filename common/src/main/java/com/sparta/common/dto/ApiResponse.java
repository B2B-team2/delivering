package com.sparta.common.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({ "status", "message", "data" })
public class ApiResponse<T> {
    
    private int status;
    private String message;
    private T data;

    // 성공 응답 (데이터가 있는 경우)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "SUCCESS", data);
    }

    // 성공 응답 (데이터가 없는 경우, 예: 삭제 완료)
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(200, "SUCCESS", null);
    }

    // 생성 성공 응답 (201 Created)
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "CREATED", data);
    }
}
