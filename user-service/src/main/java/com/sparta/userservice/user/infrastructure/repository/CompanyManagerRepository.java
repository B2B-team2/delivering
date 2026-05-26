package com.sparta.userservice.user.infrastructure.repository;

import com.sparta.userservice.user.domain.entity.CompanyManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyManagerRepository extends JpaRepository<CompanyManager, UUID> {
}
