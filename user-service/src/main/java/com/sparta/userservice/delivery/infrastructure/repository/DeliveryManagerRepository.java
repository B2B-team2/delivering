package com.sparta.userservice.delivery.infrastructure.repository;

import com.sparta.userservice.user.domain.entity.DeliveryManager;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import com.sparta.userservice.user.domain.enums.ManagerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerRepository extends JpaRepository<DeliveryManager, UUID> {

    Page<DeliveryManager> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<DeliveryManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    int countByDeletedAtIsNull();

    Optional<DeliveryManager> findFirstByManagerTypeAndStatusAndDeletedAtIsNullOrderByDeliveryOrderAsc(
            ManagerType managerType, DeliveryManagerStatus status);

    @Query("SELECT dm FROM DeliveryManager dm JOIN FETCH dm.user WHERE dm.deletedAt IS NULL")
    Page<DeliveryManager> findAllWithUser(Pageable pageable);

    @Query("SELECT dm FROM DeliveryManager dm JOIN FETCH dm.user WHERE dm.userId = :userId AND dm.deletedAt IS NULL")
    Optional<DeliveryManager> findByUserIdWithUser(@Param("userId") UUID userId);

    @Query("SELECT dm FROM DeliveryManager dm JOIN FETCH dm.user WHERE dm.managerType = :managerType AND dm.status = :status AND dm.deletedAt IS NULL ORDER BY dm.deliveryOrder ASC")
    Optional<DeliveryManager> findFirstByManagerTypeAndStatusWithUser(@Param("managerType") ManagerType managerType, @Param("status") DeliveryManagerStatus status);

    @Query("SELECT dm FROM DeliveryManager dm JOIN FETCH dm.user " +
            "WHERE dm.deletedAt IS NULL " +
            "AND (:managerType IS NULL OR dm.managerType = :managerType) " +
            "AND (:status IS NULL OR dm.status = :status) " +
            "AND (:hubId IS NULL OR dm.hubId = :hubId)")
    Page<DeliveryManager> searchWithUser(
            @Param("managerType") ManagerType managerType,
            @Param("status") DeliveryManagerStatus status,
            @Param("hubId") UUID hubId,
            Pageable pageable);

    @Query("SELECT dm FROM DeliveryManager dm JOIN FETCH dm.user " +
            "WHERE dm.hubId = :hubId AND dm.managerType = :managerType " +
            "AND dm.status = :status AND dm.deletedAt IS NULL " +
            "ORDER BY dm.deliveryOrder ASC")
    Optional<DeliveryManager> findFirstByHubIdAndManagerTypeAndStatusWithUser(
            @Param("hubId") UUID hubId,
            @Param("managerType") ManagerType managerType,
            @Param("status") DeliveryManagerStatus status);
}