package com.trinity.ctc.domain.notification.dto;

import com.trinity.ctc.domain.restaurant.entity.RestaurantImage;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Schema(description = "알림 신청 좌석 정보")
public class SubscriptionResponse {

    @Schema(description = "빈자리 알림 ID", example = "1")
    private long seatNotificationId;

    @Schema(description = "식당 이름", example = "이가네양꼬치")
    private String restaurantName;

    @ArraySchema(schema = @Schema(description = "식당 카테고리 리스트", example = "중식, 일식"))
    private List<String> restaurantCategory;

    @ArraySchema(schema = @Schema(description = "식당 사진 URL 리스트", example = "img1.kakaocdn.net/..., img2.kakaocdn.net/..."))
    private List<String> restaurantImageUrl;

    @Schema(description = "빈자리 예약 날짜", example = "2025-02-26")
    private LocalDate date;

    @Schema(description = "빈자리 예약 시간", example = "09:00:00")
    private LocalTime timeSlot;

    @Schema(description = "최소 인원", example = "2")
    private int minCapacity;

    @Schema(description = "최대 인원", example = "4")
    private int maxCapacity;

    @Schema(description = "빈자리 알림 신청자 수", example = "13")
    private int subscriberCount;

    public static SubscriptionResponse of (long seatNotificationId, SeatAvailability seatAvailability, int subscriberCount) {
        return new SubscriptionResponse(seatNotificationId,
                seatAvailability.getRestaurant().getName(),
                seatAvailability.getRestaurant().getRestaurantCategoryList().stream()
                        .map(restaurantCategory -> restaurantCategory.getCategory().getName()).collect(Collectors.toList()),
                seatAvailability.getRestaurant().getImageUrls().stream().map(RestaurantImage::getUrl).collect(Collectors.toList()),
                seatAvailability.getReservationDate(),
                seatAvailability.getReservationTime().getTimeSlot(),
                seatAvailability.getSeatType().getMinCapacity(),
                seatAvailability.getSeatType().getMaxCapacity(),
                subscriberCount
        );
    }
}
