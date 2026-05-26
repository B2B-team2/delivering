package com.sparta.userservice.user.infrastructure.repository;

import com.sparta.userservice.user.domain.entity.HubManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HubManagerRepository extends JpaRepository<HubManager, UUID> {
}