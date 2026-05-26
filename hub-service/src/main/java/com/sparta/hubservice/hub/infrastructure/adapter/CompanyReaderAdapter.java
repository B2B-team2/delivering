package com.sparta.hubservice.hub.infrastructure.adapter;

import com.sparta.hubservice.global.client.CompanyClient;
import com.sparta.hubservice.hub.domain.port.CompanyReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyReaderAdapter implements CompanyReader {

    private final CompanyClient companyClient;

    @Override
    public boolean existsCompaniesByHubId(UUID hubId) {
        return Boolean.TRUE.equals(companyClient.existsCompaniesByHubId(hubId));
    }
}
