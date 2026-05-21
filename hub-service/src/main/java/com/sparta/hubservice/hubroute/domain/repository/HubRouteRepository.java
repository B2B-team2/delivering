package com.sparta.hubservice.hubroute.domain.repository;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository {

    HubRoute save(HubRoute hubRoute);

    Optional<HubRoute> findById(UUID routeId);

    Optional<HubRoute> findByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);

    List<HubRoute> findAll();

    List<HubRoute> findByFromHubId(UUID fromHubId);

    void delete(HubRoute hubRoute);
}
