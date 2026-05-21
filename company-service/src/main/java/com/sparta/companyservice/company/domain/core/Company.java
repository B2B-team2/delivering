package com.sparta.companyservice.company.domain.core;
import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.UUID;

@Entity
@Table(name = "p_companies", schema = "company-db")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Company extends BaseEntity {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID companyId;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyTypeEnum companyType;

    private String phone;

    private String description;

    @Column(nullable = false)
    private String businessNumber;

    @Column(nullable = false)
    private UUID hubId;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    private String address;

    @Column(name = "logo_url")
    private String logoUrl;

    @Builder
    public Company(UUID companyId, String companyName, CompanyTypeEnum companyType, String phone,
                  String description, String businessNumber, UUID hubId, Double latitude,
                  Double longitude, String address, String logoUrl) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyType = companyType;
        this.phone = phone;
        this.description = description;
        this.businessNumber = businessNumber;
        this.hubId = hubId;
        this.location = (latitude != null && longitude != null) 
            ? geometryFactory.createPoint(new Coordinate(longitude, latitude)) 
            : null;
        this.address = address;
        this.logoUrl = logoUrl;
    }

    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }
}
