package com.sparta.hubservice.hubroute.infrastructure.repository;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import com.sparta.hubservice.hubroute.domain.repository.HubRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepository {

    private final HubRouteJpaRepository hubRouteJpaRepository;

    @Override
    public HubRoute save(HubRoute hubRoute) {
        return hubRouteJpaRepository.save(hubRoute);
    }

    @Override
    public Optional<HubRoute> findById(UUID routeId) {
        return hubRouteJpaRepository.findById(routeId);
    }

    @Override
    public Optional<HubRoute> findByFromHubIdAndToHubId(UUID fromHubId, UUID toHubId) {
        return hubRouteJpaRepository.findByFromHubIdAndToHubId(fromHubId, toHubId);
    }

    @Override
    public List<HubRoute> findByFromHubId(UUID fromHubId) {
        return hubRouteJpaRepository.findByFromHubId(fromHubId);
    }

    @Override
    public void delete(HubRoute hubRoute) {
        hubRouteJpaRepository.delete(hubRoute);
    }
}
