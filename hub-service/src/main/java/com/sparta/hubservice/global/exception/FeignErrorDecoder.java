package com.sparta.hubservice.global.exception;

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
        return new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}
