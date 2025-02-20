package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantPreviewResponse {

    private Long restaurantId;
    private String name;
    private double rating;
    private String category;
    private String location;
    private String operatingDays;
    private String operatingHours;
    private List<String> imageUrls;
    private int averagePrice;
    private boolean isWishlisted;
    private List<ReservationAvailabilityResponse> reservation; // 날짜별 예약 가능 여부 리스트로 변경


    public static RestaurantPreviewResponse fromEntity(Restaurant restaurant, boolean isWishlisted, List<ReservationAvailabilityResponse> reservation) {

        return RestaurantPreviewResponse.builder()
            .restaurantId(restaurant.getId())
            .name(restaurant.getName())
            .rating(restaurant.getRating())
            .category(restaurant.getRestaurantCategoryList().stream()
                .map(rc -> rc.getCategory().getName())
                .collect(Collectors.joining(", ")))
            .location(restaurant.getAddress())
            .operatingDays(restaurant.getExpandedDays())
            .operatingHours(restaurant.getOperatingHour())
            .imageUrls(restaurant.getImageUrls().stream()
                .map(RestaurantImage::getUrl)
                .collect(Collectors.toList()))
            .averagePrice(restaurant.getAveragePrice())
            .isWishlisted(isWishlisted)
            .reservation(reservation)
            .build();
    }
}
