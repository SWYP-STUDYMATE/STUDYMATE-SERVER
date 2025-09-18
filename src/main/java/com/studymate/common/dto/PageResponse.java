package com.studymate.common.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * Spring Data {@link Page}를 안정적인 JSON 구조로 변환하기 위한 DTO.
 * PageImpl 직렬화 경고를 피하고, 클라이언트가 의존할 수 있는 명시적 페이징 메타데이터를 제공한다.
 */
@Getter
@Builder
public class PageResponse<T> {

    private final List<T> content;
    private final Pagination pagination;
    private final SortInfo sort;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pagination(Pagination.from(page))
                .sort(SortInfo.from(page.getSort()))
                .build();
    }

    @Getter
    @Builder
    public static class Pagination {
        private final int page; // 0-based index
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final int numberOfElements;
        private final boolean first;
        private final boolean last;
        private final boolean hasNext;
        private final boolean hasPrevious;
        private final boolean empty;

        private static Pagination from(Page<?> page) {
            return Pagination.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .numberOfElements(page.getNumberOfElements())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .empty(page.isEmpty())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SortInfo {
        private final boolean sorted;
        private final boolean unsorted;
        private final boolean empty;
        private final List<Order> orders;

        private static SortInfo from(Sort sort) {
            if (sort == null) {
                return SortInfo.builder()
                        .sorted(false)
                        .unsorted(true)
                        .empty(true)
                        .orders(Collections.emptyList())
                        .build();
            }

            List<Order> orders = sort.stream()
                    .map(Order::from)
                    .collect(Collectors.toList());

            return SortInfo.builder()
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .empty(sort.isEmpty())
                    .orders(orders)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Order {
        private final String property;
        private final Sort.Direction direction;
        private final boolean ignoreCase;
        private final Sort.NullHandling nullHandling;

        private static Order from(Sort.Order order) {
            return Order.builder()
                    .property(order.getProperty())
                    .direction(order.getDirection())
                    .ignoreCase(order.isIgnoreCase())
                    .nullHandling(order.getNullHandling())
                    .build();
        }
    }
}
