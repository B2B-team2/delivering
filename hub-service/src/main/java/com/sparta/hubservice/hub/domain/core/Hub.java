package com.sparta.hubservice.hub.domain.core;

import com.sparta.common.entity.BaseEntity;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.UUID;

@Entity
@Table(name = "p_logistics_hubs", schema = "hub-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

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
    private Point location;

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
        this.location = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        this.contactPhone = contactPhone;
        this.status = status;
    }

    public Double getLatitude() {
        return location.getY();
    }

    public Double getLongitude() {
        return location.getX();
    }

    public void update(String name, String address, Double latitude, Double longitude,
                       String contactPhone, HubStatus status) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (latitude != null && longitude != null)
            this.location = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (status != null) this.status = status;
    }
}
