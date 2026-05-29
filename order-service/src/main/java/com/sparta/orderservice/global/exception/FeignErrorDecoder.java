package com.sparta.orderservice.global.exception;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.order.infrastructure.client.CompanyClient;
import com.sparta.orderservice.order.infrastructure.client.DeliveryClient;
import com.sparta.orderservice.order.infrastructure.client.HubClient;
import com.sparta.orderservice.order.infrastructure.client.ProductClient;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 전역 Feign 에러 디코더 (팀 컨벤션: 서비스별 단일 전역 디코더)
 *
 * methodKey 형식: "ClassName#methodName(ParamTypes)" — Feign이 자동 생성
 *
 * [클라이언트 식별 방식]
 * 문자열 리터럴 대신 .class.getSimpleName() 상수로 참조하여
 * 클라이언트 클래스명 변경 시 컴파일 에러로 즉시 탐지되도록 보완.
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    // 클래스 참조로 선언 → 클라이언트 클래스명 변경 시 컴파일 에러로 즉시 탐지
    private static final String HUB_CLIENT      = HubClient.class.getSimpleName();
    private static final String COMPANY_CLIENT  = CompanyClient.class.getSimpleName();
    private static final String DELIVERY_CLIENT = DeliveryClient.class.getSimpleName();
    private static final String PRODUCT_CLIENT  = ProductClient.class.getSimpleName();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("[Feign] methodKey={}, status={}, reason={}", methodKey, response.status(), response.reason());

        if (methodKey.contains(HUB_CLIENT)) {
            return decodeHubError(response);
        } else if (methodKey.contains(COMPANY_CLIENT)) {
            return decodeCompanyError(response);
        } else if (methodKey.contains(DELIVERY_CLIENT)) {
            return decodeDeliveryError(response);
        } else if (methodKey.contains(PRODUCT_CLIENT)) {
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
