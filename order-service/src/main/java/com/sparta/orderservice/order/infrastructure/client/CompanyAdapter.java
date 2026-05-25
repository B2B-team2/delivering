package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.DeliveryAddressInfo;
import com.sparta.orderservice.order.application.port.CompanyPort;
import com.sparta.orderservice.order.infrastructure.client.dto.DefaultDeliveryAddressResponse;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingResponse;
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
public class CompanyAdapter implements CompanyPort {

    private final CompanyClient companyClient;

    // companyId 목록을 일괄 조회하여 { companyId → hubId } Map으로 반환
    @Override
    public Map<UUID, UUID> getHubIds(List<UUID> companyIds) {
        try {
            HubMappingResponse response = companyClient.getHubMapping(new HubMappingRequest(companyIds));
            // response.mappings() = { "companyId 문자열" : { companyId, hubId, companyName } }
            // JSON 키는 String -> UUID로 변환하여 Map<UUID, UUID> 형태로 재구성
            // 결과: { companyId(UUID) → hubId(UUID) }
            return response.mappings().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> UUID.fromString(e.getKey()),  // 키: String → UUID 변환
                            e -> e.getValue().hubId()           // 값: CompanyHubInfo에서 hubId만 추출
                    ));
        } catch (FeignException.NotFound e) {
            throw new BusinessException(OrderErrorCode.HUB_MAPPING_NOT_FOUND); // 404: 업체 없음
        } catch (FeignException e) {
            handleCompanyFeignException("getHubIds", e);
            return null; // unreachable — 컴파일러를 위한 명시적 반환
        } catch (Exception e) {
            handleCompanyUnexpectedException("getHubIds", e);
            return null; // unreachable
        }
    }

    // 수령업체의 기본 배송지(is_default=true) 조회
    // infrastructure DTO(DefaultDeliveryAddressResponse)를 application DTO(DeliveryAddressInfo)로 변환하여 반환
    @Override
    public DeliveryAddressInfo getDefaultDeliveryAddress(UUID receiverCompanyId) {
        try {
            DefaultDeliveryAddressResponse response = companyClient.getDefaultDeliveryAddress(receiverCompanyId);
            return new DeliveryAddressInfo(
                    response.address(),
                    response.addressDetail(),
                    response.recipientName(),
                    response.phone()
            );
        } catch (FeignException.NotFound e) {
            throw new BusinessException(OrderErrorCode.COMPANY_NOT_FOUND); // 404: 업체 없음
        } catch (FeignException e) {
            handleCompanyFeignException("getDefaultDeliveryAddress", e);
            return null; // unreachable
        } catch (Exception e) {
            handleCompanyUnexpectedException("getDefaultDeliveryAddress", e);
            return null; // unreachable
        }
    }

    private void handleCompanyFeignException(String operation, FeignException e) {
        log.error("Company service error [{}]: status={}", operation, e.status());
        throw new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
    }

    private void handleCompanyUnexpectedException(String operation, Exception e) {
        log.error("Unexpected error [{}]", operation, e);
        throw new BusinessException(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE);
    }
}
