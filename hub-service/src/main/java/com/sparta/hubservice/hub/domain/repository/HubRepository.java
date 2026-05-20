package com.sparta.hubservice.hub.domain.repository;

import com.sparta.hubservice.hub.domain.core.Hub;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubRepository {

    Hub save(Hub hub);

    Optional<Hub> findById(UUID hubId);

    List<Hub> findAll();

    void delete(Hub hub);
}
