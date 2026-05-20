package com.sparta.hubservice.hub.infrastructure.repository;

import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class HubRepositoryImpl implements HubRepository {

    private final HubJpaRepository hubJpaRepository;

    @Override
    public Hub save(Hub hub) {
        return hubJpaRepository.save(hub);
    }

    @Override
    public Optional<Hub> findById(UUID hubId) {
        return hubJpaRepository.findByHubIdAndDeletedAtIsNull(hubId);
    }

    @Override
    public List<Hub> findAll() {
        return hubJpaRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public void delete(Hub hub) {
        hubJpaRepository.delete(hub);
    }
}
