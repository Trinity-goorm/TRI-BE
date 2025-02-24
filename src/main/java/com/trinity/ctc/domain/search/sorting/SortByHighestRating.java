package com.trinity.ctc.domain.search.sorting;

import org.springframework.data.domain.Sort;

public class SortByHighestRating implements SortingStrategy {
    @Override
    public Sort getSort() {
        return Sort.by(Sort.Order.desc("rating"));
    }
}
