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
        log.error("Feign Error: methodKey {}, status {}, reason {}", methodKey, response.status(), response.reason());

        // HTTP 상태 코드에 따른 구체적인 에러 매핑
        return switch (response.status()) {
            case 404 -> new BusinessException(OrderErrorCode.COMPANY_NOT_FOUND);
            default -> new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
        };
    }
}
