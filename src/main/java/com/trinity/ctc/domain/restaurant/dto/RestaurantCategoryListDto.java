package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityDto;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import java.util.List;
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
    private int averagePrice;
    private boolean isWishlisted;
    private List<ReservationAvailabilityDto> reservation; // 날짜별 예약 가능 여부 리스트로 변경


    public static RestaurantCategoryListDto fromEntity(Restaurant restaurant, boolean isWishlisted, List<ReservationAvailabilityDto> reservation) {
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
            .isWishlisted(isWishlisted)
            .reservation(reservation)
            .build();
    }

    private static int calculateAveragePrice(Restaurant restaurant) {
        return (int) restaurant.getMenus().stream()
            .mapToInt(menu -> menu.getPrice())
            .average()
            .orElse(0.0);
    }

}
