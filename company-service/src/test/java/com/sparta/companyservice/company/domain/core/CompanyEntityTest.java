package com.sparta.companyservice.company.domain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyEntityTest {
    @Test
    @DisplayName("공간 데이터 통합 변환 검증: 위도/경도가 단일 Point(location, SRID 4326)로 정확히 변환되는가?")
    void geometryIntegrationConversionTest() {
        // given
        Double lat = 37.5665;
        Double lon = 126.9780;

        // when
        Company company = Company.builder()
                .latitude(lat)
                .longitude(lon)
                .build();

        // then
        assertThat(company.getLocation()).isNotNull();
        assertThat(company.getLocation().getSRID()).isEqualTo(4326);
        assertThat(company.getLocation().getY()).isEqualTo(lat);
        assertThat(company.getLocation().getX()).isEqualTo(lon);
        
        // 편의 메서드 검증
        assertThat(company.getLatitude()).isEqualTo(lat);
        assertThat(company.getLongitude()).isEqualTo(lon);
    }
}
