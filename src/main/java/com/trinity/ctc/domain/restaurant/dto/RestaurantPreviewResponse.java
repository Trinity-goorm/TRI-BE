package com.trinity.ctc.domain.restaurant.dto;

import com.trinity.ctc.domain.reservation.dto.ReservationAvailabilityResponse;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@Schema(description = "식당 미리보기 정보 반환")
public class RestaurantPreviewResponse {

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "식당 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "식당 이름", example = "캐치핑 식당")
    private String name;

    @Schema(description = "식당 평점", example = "4.5")
    private double rating;

    @Schema(description = "식당 카테고리", example = "한식")
    private List<String> category;

    @Schema(description = "식당 위치", example = "경기 성남시 분당구 대왕판교로606번길 58 판교푸르지오월드마크 2033호")
    private String location;

    @Schema(description = "운영 요일", example = "월,화,수,목,금,토,일")
    private String operatingDays;

    @Schema(description = "운영 시간", example = "매일 11:30 ~ 21:30")
    private String operatingHours;

    @Schema(description = "식당 이미지 URL 리스트", example = "[\"https://catchping.com/image1.jpg\", \"https://catchping.com/image2.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "평균 가격", example = "20000")
    private int averagePrice;

    @Schema(description = "찜 여부", example = "true")
    private boolean isWishlisted;

    @Schema(description = "날짜별 예약 가능 여부 리스트", example = "[{\"date\":\"2025-02-15\",\"isAvailable\":true},{\"date\":\"2025-02-16\",\"isAvailable\":false}]")
    private List<ReservationAvailabilityResponse> reservation; // 날짜별 예약 가능 여부 리스트로 변경



    public static RestaurantPreviewResponse fromEntity(User user, Restaurant restaurant, boolean isWishlisted, List<ReservationAvailabilityResponse> reservation, List<RestaurantCategoryName> rcList, List<Restaurant> restaurantImages) {
        return RestaurantPreviewResponse.builder()
                .userName(user.getNickname())
                .restaurantId(restaurant.getId())
                .name(restaurant.getName())
                .rating(restaurant.getRating())
                .category(restaurant.getCategories(rcList))//rclist에서 restaurantId가 같은 카테고리 이름들을 가져와야함
                .location(restaurant.getAddress())
                .operatingDays(restaurant.getExpandedDays())
                .operatingHours(restaurant.getOperatingHour())
                .imageUrls(restaurant.getRestaurantImageUrls(restaurantImages))
                .averagePrice(restaurant.getAveragePrice())
                .isWishlisted(isWishlisted)
                .reservation(reservation)
                .build();
    }
}
