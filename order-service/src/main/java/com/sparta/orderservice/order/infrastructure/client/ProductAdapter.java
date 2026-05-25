package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.ProductOptionInfo;
import com.sparta.orderservice.order.application.port.ProductPort;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoItem;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

    private final ProductClient productClient;

    // 상품 옵션 ID 목록 → { productOptionId(UUID): ProductOptionInfo } Map 반환
    // infrastructure DTO(ProductOptionInfoItem)를 application DTO(ProductOptionInfo)로 변환하여 반환
    @Override
    public Map<UUID, ProductOptionInfo> getProductOptionInfos(List<UUID> productOptionIds) {
        try {
            // API 응답: { "optionsMap": { "uuid문자열": { productOptionId, companyId, unitPrice } } }
            // String 키를 UUID로 변환하고, 값을 application DTO로 매핑하여 반환
            Map<String, ProductOptionInfoItem> optionsMap =
                    productClient.getProductOptionInfos(new ProductOptionInfoRequest(productOptionIds))
                            .optionsMap();

            return optionsMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> UUID.fromString(e.getKey()),
                            e -> new ProductOptionInfo(
                                    e.getValue().productOptionId(),
                                    e.getValue().companyId(),
                                    e.getValue().unitPrice()
                            )
                    ));
        } catch (FeignException e) {
            handleProductFeignException("getProductOptionInfos", e);
            return null; // unreachable
        } catch (Exception e) {
            handleProductUnexpectedException("getProductOptionInfos", e);
            return null; // unreachable
        }
    }

    private void handleProductFeignException(String operation, FeignException e) {
        log.error("Product service error [{}]: status={}", operation, e.status());
        throw new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }

    private void handleProductUnexpectedException(String operation, Exception e) {
        log.error("Unexpected error [{}]", operation, e);
        throw new BusinessException(OrderErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}
