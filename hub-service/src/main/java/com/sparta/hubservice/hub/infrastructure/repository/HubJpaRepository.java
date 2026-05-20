package com.sparta.hubservice.hub.infrastructure.repository;

import com.sparta.hubservice.hub.domain.core.Hub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubJpaRepository extends JpaRepository<Hub, UUID> {

    List<Hub> findAllByDeletedAtIsNull();

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);
}
