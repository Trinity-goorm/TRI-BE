package com.trinity.ctc.domain.search.sorting;

import org.springframework.data.domain.Sort;

public class SortByLowestAveragePrice implements SortingStrategy {
    @Override
    public Sort getSort() {
        return Sort.by(Sort.Order.asc("averagePrice"));
    }
}
