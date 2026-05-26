package com.sparta.userservice.admin.infrastructure.repository;

import com.sparta.userservice.user.domain.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
}
