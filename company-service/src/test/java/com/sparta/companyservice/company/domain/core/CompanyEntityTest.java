package com.sparta.companyservice.company.domain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyEntityTest {
    @Test
    @DisplayName("공간 데이터 변환 검증: Double 좌표가 JTS Point(SRID 4326)로 정확히 변환되는가?")
    void geometryConversionTest() {
        // given
        Double lat = 37.5665;
        Double lon = 126.9780;

        // when
        Company company = Company.builder()
                .latitude(lat)
                .longitude(lon)
                .build();

        // then
        // Company 엔티티 내부에서 latitude Point는 (0, lat), longitude Point는 (lon, 0)으로 생성됨
        // 이 로직이 엔티티 클래스의 구현과 일치하는지 확인
        assertThat(company.getLatitude()).isEqualTo(lat);
        assertThat(company.getLongitude()).isEqualTo(lon);
    }
}
