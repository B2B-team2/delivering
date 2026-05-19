package com.sparta.companyservice.company.domain.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "p_companies", schema = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID companyId;

    @Column(nullable = false)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType companyType;

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

    private String logo_url;

    public enum CompanyType {
        PRODUCER, RECEIVER
    }
}
