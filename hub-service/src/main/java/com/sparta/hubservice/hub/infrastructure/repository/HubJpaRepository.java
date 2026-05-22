package com.sparta.hubservice.hub.infrastructure.repository;

import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubJpaRepository extends JpaRepository<Hub, UUID>, JpaSpecificationExecutor<Hub> {

    List<Hub> findAllByDeletedAtIsNull();

    Optional<Hub> findByHubIdAndDeletedAtIsNull(UUID hubId);

    List<Hub> findAllByHubTypeAndDeletedAtIsNull(HubType hubType);
}
