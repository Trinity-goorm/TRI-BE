package com.trinity.ctc.domain.search.dto;

import com.trinity.ctc.domain.restaurant.dto.RestaurantPreviewResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 단일 검색 방법의 성능 측정 결과
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchPerformanceSingleResult {
    private String searchType;
    private long executionTimeMs;
    private long resultCount;
    private List<RestaurantPreviewResponse> results;
}
