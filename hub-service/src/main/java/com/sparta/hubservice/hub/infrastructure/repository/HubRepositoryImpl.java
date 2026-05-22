package com.sparta.hubservice.hub.infrastructure.repository;

import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public List<Hub> findAllByHubType(HubType hubType) {
        return hubJpaRepository.findAllByHubTypeAndDeletedAtIsNull(hubType);
    }

    @Override
    public void delete(Hub hub, String deletedBy) {
        hub.softDelete(deletedBy);
        hubJpaRepository.save(hub);
    }
}
