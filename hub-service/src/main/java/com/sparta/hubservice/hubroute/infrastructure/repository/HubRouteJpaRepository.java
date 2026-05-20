package com.sparta.hubservice.hubroute.infrastructure.repository;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRouteJpaRepository extends JpaRepository<HubRoute, UUID> {

    Optional<HubRoute> findByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId);

    List<HubRoute> findByFromHubId(UUID fromHubId);
}
