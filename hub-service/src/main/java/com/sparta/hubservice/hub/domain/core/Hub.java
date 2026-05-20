package com.sparta.hubservice.hub.domain.core;

import com.sparta.common.entity.BaseEntity;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_logistics_hubs", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_id")
    private UUID hubId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "hub_type")
    private HubType hubType;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    private HubStatus status;

    @Builder
    public Hub(String name, HubType hubType, String address, Double latitude, Double longitude,
               String contactPhone, HubStatus status) {
        this.name = name;
        this.hubType = hubType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactPhone = contactPhone;
        this.status = status;
    }

    public void update(String name, String address, Double latitude, Double longitude,
                       String contactPhone, HubStatus status) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (status != null) this.status = status;
    }
}
