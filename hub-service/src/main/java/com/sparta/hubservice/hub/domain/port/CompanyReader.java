package com.sparta.hubservice.hub.domain.port;

import java.util.UUID;

public interface CompanyReader {
    boolean existsCompaniesByHubId(UUID hubId);
}
