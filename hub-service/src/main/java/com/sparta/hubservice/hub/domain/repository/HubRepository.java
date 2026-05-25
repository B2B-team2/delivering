package com.sparta.hubservice.hub.domain.repository;

import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRepository {

    Hub save(Hub hub);

    Optional<Hub> findById(UUID hubId);

    List<Hub> findAll();

    List<Hub> findAllByHubType(HubType hubType);

    Page<Hub> search(HubType hubType, HubStatus status, String keyword, Pageable pageable);

    void delete(Hub hub, UUID deletedBy);
}
