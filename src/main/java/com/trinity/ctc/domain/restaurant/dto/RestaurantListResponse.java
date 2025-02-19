package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantListResponse {

    private Long restaurantId;
    private String name;
    private double rating;
    private String category;
    private String location;
    private String operatingHours;
    private List<RestaurantImagesResponse> imageUrls;
    private int averagePrice;
    private boolean isWishlisted;
    private List<ReservationAvailabilityResponse> reservation; // 날짜별 예약 가능 여부 리스트로 변경


    public static RestaurantListResponse fromEntity(Restaurant restaurant, boolean isWishlisted, List<ReservationAvailabilityResponse> reservation) {
        List<RestaurantImagesResponse> imageUrls =
            restaurant.getImageUrls().stream()
            .map(ri -> new RestaurantImagesResponse(ri.getUrl()))
            .collect(Collectors.toList());

        return RestaurantListResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .rating(restaurant.getRating())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .location(restaurant.getAddress())
            .operatingHours(restaurant.getOperatingHour())
            .imageUrls(imageUrls)
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
