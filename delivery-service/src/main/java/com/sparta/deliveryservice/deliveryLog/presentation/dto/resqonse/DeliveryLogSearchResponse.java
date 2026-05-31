package com.sparta.deliveryservice.deliveryLog.presentation.dto.resqonse;

import com.sparta.common.dto.PageResponse;
import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLogStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.UUID;

@Getter
public class DeliveryLogSearchResponse {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeliveryLogWrapperDto {
        private UUID deliveryId;
        private PageResponse<DeliveryLogResponseDto> pageData;

    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryLogResponseDto {
        private UUID logId;
        private UUID routeId;
        private DeliveryLogStatus eventType;
        private Object previousValue;
        private Object currentValue;
        private String reason;
    }

    public static PageResponse<DeliveryLogResponseDto> of(Page<DeliveryLogResponseDto> pageInfo) {
        return new PageResponse<>(pageInfo);
    }
}