package com.trinity.ctc.global.util.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SortOrder {

    RESERVE_DATE_DESC(Sort.Direction.DESC, "reservationDate"),
    RESERVE_DATE_ASC(Sort.Direction.ASC, "reservationDate");

    private final Sort.Direction direction;
    private final String targetField;

    public static SortOrder fromString(String direction) {
        return Arrays.stream(SortOrder.values())
                .filter(sortOrder -> sortOrder.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElse(RESERVE_DATE_DESC);
    }

    public Sort getSort() {
        return Sort.by(direction, targetField);
    }
}
