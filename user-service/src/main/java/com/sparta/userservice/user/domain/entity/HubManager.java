package com.sparta.userservice.user.domain.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_hub_managers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubManager extends BaseEntity {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "hub_id")
    private UUID hubId;

    public static HubManager create(User user) {
        HubManager hubManager = new HubManager();
        hubManager.user = user;
        return hubManager;
    }

    public void assignHub(UUID hubId) {
        this.hubId = hubId;
    }
}