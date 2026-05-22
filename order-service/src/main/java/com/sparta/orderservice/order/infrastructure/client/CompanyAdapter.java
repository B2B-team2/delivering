package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.application.port.CompanyPort;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompanyAdapter implements CompanyPort {

    private final CompanyClient companyClient;

    // companyId 목록을 일괄 조회하여 { companyId → hubId } Map으로 반환
    @Override
    public Map<UUID, UUID> getHubIds(List<UUID> companyIds) {
        HubMappingResponse response = companyClient.getHubMapping(new HubMappingRequest(companyIds)).getData();
        // response.mappings() = { "companyId 문자열" : { companyId, hubId, companyName } }
        // JSON 키는 String -> UUID로 변환하여 Map<UUID, UUID> 형태로 재구성
        // 결과: { companyId(UUID) → hubId(UUID) }
        return response.mappings().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> UUID.fromString(e.getKey()),  // 키: String → UUID 변환
                        e -> e.getValue().hubId()           // 값: CompanyHubInfo에서 hubId만 추출
                ));
    }
}
