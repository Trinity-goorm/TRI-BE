package com.trinity.ctc.domain.search.sorting;

import org.springframework.data.domain.Sort;

public interface SortingStrategy {
    Sort getSort();

}
