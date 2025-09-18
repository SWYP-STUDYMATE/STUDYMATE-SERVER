package com.studymate.common.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PageResponseTest {

    @Test
    @DisplayName("PageResponse.of()는 Page 메타데이터를 안정적으로 매핑한다")
    void of_ShouldMapPaginationMetadata() {
        // given
        PageRequest pageable = PageRequest.of(2, 5, Sort.by(Sort.Order.desc("createdAt")));
        Page<String> page = new PageImpl<>(List.of("A", "B"), pageable, 17);

        // when
        PageResponse<String> response = PageResponse.of(page);

        // then
        assertThat(response.getContent()).containsExactly("A", "B");
        assertThat(response.getPagination().getPage()).isEqualTo(2);
        assertThat(response.getPagination().getSize()).isEqualTo(5);
        assertThat(response.getPagination().getTotalElements()).isEqualTo(17);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(4);
        assertThat(response.getPagination().isHasNext()).isTrue();
        assertThat(response.getPagination().isHasPrevious()).isTrue();
        assertThat(response.getSort().isSorted()).isTrue();
        assertThat(response.getSort().getOrders()).hasSize(1);
        assertThat(response.getSort().getOrders().getFirst().getProperty()).isEqualTo("createdAt");
        assertThat(response.getSort().getOrders().getFirst().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("정렬 정보가 없는 Page도 안전하게 직렬화된다")
    void of_ShouldHandleUnsortedPages() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Integer> page = new PageImpl<>(List.of(), pageable, 0);

        // when
        PageResponse<Integer> response = PageResponse.of(page);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPagination().isEmpty()).isTrue();
        assertThat(response.getPagination().isFirst()).isTrue();
        assertThat(response.getSort().isSorted()).isFalse();
        assertThat(response.getSort().getOrders()).isEmpty();
    }
}
