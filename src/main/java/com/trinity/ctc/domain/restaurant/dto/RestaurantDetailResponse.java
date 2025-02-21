package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;

import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RestaurantDetailResponse {

    private Long restaurantId;
    private String name;
    private List<String> imageUrls;
    private String location;
    private String category;
    private double rating;
    private int averagePrice;
    private String expandedDays;
    private String timeRange;
    private List<String> facilities;
    private List<String> cautions;
    private List<MenuDto> menus;
    private int wishCount;

    public static RestaurantDetailResponse fromEntity(Restaurant restaurant) {
        // 식당 운영시간
        return RestaurantDetailResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .imageUrls(restaurant.getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()))
            .location(restaurant.getAddress())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .rating(restaurant.getRating())
            .averagePrice(restaurant.getAveragePrice())
            .expandedDays(restaurant.getExpandedDays())
            .timeRange(restaurant.getTimeRange())
            .facilities(List.of(restaurant.getConvenience().split("\n")))
            .cautions(List.of(restaurant.getCaution().split(", ")))
            .menus(restaurant.getMenus().stream().map(MenuDto::fromEntity).collect(Collectors.toList()))
            .wishCount(restaurant.getLikeList().size())
            .build();
    }

    public static RestaurantDetailResponse fromLike(Restaurant restaurant) {
        return RestaurantDetailResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .imageUrls(restaurant.getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()))
            .location(restaurant.getAddress())
            .averagePrice(restaurant.getAveragePrice())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .rating(restaurant.getRating())
            .build();
    }
}

