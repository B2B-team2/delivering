package com.sparta.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageableUtilTest {

    @Test
    @DisplayName("허용된 사이즈 유지: 10, 30, 50인 경우 입력값이 그대로 유지되는가?")
    void validatePageSizeAllowedTest() {
        // given
        Pageable size10 = PageRequest.of(0, 10);
        Pageable size30 = PageRequest.of(1, 30);
        Pageable size50 = PageRequest.of(2, 50);

        // when & then
        assertThat(PageableUtil.validatePageSize(size10).getPageSize()).isEqualTo(10);
        assertThat(PageableUtil.validatePageSize(size30).getPageSize()).isEqualTo(30);
        assertThat(PageableUtil.validatePageSize(size50).getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("허용되지 않은 사이즈 보정: 10, 30, 50이 아닌 경우 10으로 보정되는가?")
    void validatePageSizeInvalidTest() {
        // given
        Pageable size20 = PageRequest.of(0, 20);
        Pageable size100 = PageRequest.of(0, 100);
        Pageable size5 = PageRequest.of(1, 5, Sort.by("name"));

        // when & then
        assertThat(PageableUtil.validatePageSize(size20).getPageSize()).isEqualTo(10);
        assertThat(PageableUtil.validatePageSize(size100).getPageSize()).isEqualTo(10);
        
        Pageable validated = PageableUtil.validatePageSize(size5);
        assertThat(validated.getPageSize()).isEqualTo(10);
        assertThat(validated.getPageNumber()).isEqualTo(1);
        assertThat(validated.getSort()).isEqualTo(Sort.by("name"));
    }
}
