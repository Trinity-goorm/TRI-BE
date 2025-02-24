package com.trinity.ctc.domain.search.sorting;

public class SortingStrategyFactory {
    public static SortingStrategy getStrategy(String sortType) {
        return switch (sortType) {
            case "highest_average_price" -> new SortByHighestAveragePrice();
            case "lowest_average_price" -> new SortByLowestAveragePrice();
            case "highest_rating" -> new SortByHighestRating();
            default -> throw new IllegalStateException("Unexpected value: " + sortType);
        };
    }
}

