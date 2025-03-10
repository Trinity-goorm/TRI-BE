package com.trinity.ctc.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchPerformanceResult {
    private List<SearchPerformanceSingleResult> performanceResults;
    private String keyword;
}