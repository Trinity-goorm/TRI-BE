package com.trinity.ctc.restaurant.dto;

import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantCategoryListDto {

    private Long restaurantId;
    private String name;
    private double rating;
    private String category;
    private String location;
    private String operatingHours;
    private String imageUrl;
    private double averagePrice;
    private boolean isWishlisted; // 사용자 찜 여부

    public static RestaurantCategoryListDto fromEntity(Restaurant restaurant, boolean isWishlisted) {
        return RestaurantCategoryListDto.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .rating(restaurant.getRating())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .location(restaurant.getAddress())
            .operatingHours(restaurant.getOperatingHour())
            .imageUrl(restaurant.getImageUrls().stream()
                .findFirst().map(RestaurantImage::getUrl).orElse(null))
            .averagePrice(calculateAveragePrice(restaurant))
            .isWishlisted(isWishlisted) // 사용자 찜 여부 추가
            .build();
    }

    private static double calculateAveragePrice(Restaurant restaurant) {
        return restaurant.getMenus().stream()
            .mapToInt(menu -> menu.getPrice())
            .average()
            .orElse(0.0);
    }

}
