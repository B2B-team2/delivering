package com.sparta.deliveryservice.deliveryLog.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.sparta.common.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeliveryLogResponse {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryLogResponseDto {
        private UUID logId;
        private UUID routeId;
        private String eventType;
        private Object previousValue;
        private Object currentValue;
        private String reason;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    public static PageResponse<DeliveryLogResponseDto> of(Page<DeliveryLogResponseDto> pageInfo) {
        return new PageResponse<>(pageInfo);
    }
}