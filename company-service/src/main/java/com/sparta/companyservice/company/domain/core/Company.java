package com.sparta.companyservice.company.domain.core;
import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
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
    private Point latitude;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point longitude;

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
        this.latitude = latitude != null ? geometryFactory.createPoint(new Coordinate(0, latitude)) : null;
        this.longitude = longitude != null ? geometryFactory.createPoint(new Coordinate(longitude, 0)) : null;
        this.address = address;
        this.logoUrl = logoUrl;
    }

    public Double getLatitude() {
        return latitude != null ? latitude.getY() : null;
    }

    public Double getLongitude() {
        return longitude != null ? longitude.getX() : null;
    }
}
