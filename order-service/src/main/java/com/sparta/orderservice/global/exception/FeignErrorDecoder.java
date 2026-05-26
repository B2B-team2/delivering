package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[Feign] methodKey={}, status={}, reason={}", methodKey, response.status(), response.reason());

        if (methodKey.contains("HubClient")) {
            return decodeHubError(response);
        } else if (methodKey.contains("CompanyClient")) {
            return decodeCompanyError(response);
        } else if (methodKey.contains("DeliveryClient")) {
            return decodeDeliveryError(response);
        } else if (methodKey.contains("ProductClient")) {
            return decodeProductError(response);
        }
        return new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }

    // hub-service: 409 → 재고 부족, 그 외 → 서비스 불가
    private Exception decodeHubError(Response response) {
        return switch (response.status()) {
            case 409 -> new BusinessException(OrderErrorCode.STOCK_INSUFFICIENT);
            default  -> new BusinessException(OrderErrorCode.HUB_SERVICE_UNAVAILABLE);
        };
    }

    // company-service: 404 → 업체 없음, 그 외 → 서비스 불가
    private Exception decodeCompanyError(Response response) {
        return switch (response.status()) {
            case 404 -> new BusinessException(OrderErrorCode.COMPANY_NOT_FOUND);
            default  -> new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
        };
    }

    // delivery-service: 모든 오류 → 서비스 불가
    private Exception decodeDeliveryError(Response response) {
        return new BusinessException(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE);
    }

    // product-service: 모든 오류 → 외부 서비스 오류
    private Exception decodeProductError(Response response) {
        return new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}
