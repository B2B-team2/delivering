package com.sparta.hubservice.hubroute.domain.repository;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository {

    HubRoute save(HubRoute hubRoute);

    Optional<HubRoute> findById(UUID routeId);

    Optional<HubRoute> findByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);

    List<HubRoute> findAll();

    Page<HubRoute> findAll(Pageable pageable);

    List<HubRoute> findByFromHubId(UUID fromHubId);

    void delete(HubRoute hubRoute, String deletedBy);
}
