package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.restaurant.dto.RestaurantCategoryName;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.dto.SeatTypeInfoResponse;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import com.trinity.ctc.global.util.validator.DateTimeValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "사용자 예약 하나의 정보")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserReservationResponse {

    @Schema(description = "예약 ID", example = "1400")
    private final long reservationId;

    @Schema(description = "식당 이름", example = "맛있는 식당")
    private final String restaurantName;

    @Schema(description = "식당 카테고리 리스트", example = "[\"중식\", \"양꼬치\"]")
    private final List<String> restaurantCategories;

    @Schema(description = "식당 이미지 리스트", example = "[\"imageUrl1\", \"imageUrl2\"]")
    private final List<String> restaurantImages;

    @Schema(description = "예약일자", example = "2025-02-01")
    private final String reservationDate;

    @Schema(description = "예약시간", example = "09:00")
    private final String reservationTime;

    @Schema(description = "예약상태", example = "COMPLETED")
    private final ReservationStatus status;

    @Schema(description = "만료여부", example = "true")
    private final boolean isExpired;

    @Schema(description = "좌석타입", example = "{ \"minCapacity\": 1, \"maxCapacity\": 2 }")
    private final SeatTypeInfoResponse seatType;

    public static UserReservationResponse from(Reservation reservation, List<RestaurantCategoryName> rcList, List<Restaurant> restaurantImages) {
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getRestaurant().getName(),
                reservation.getRestaurant().getCategories(rcList),
                reservation.getRestaurant().getRestaurantImageUrls(restaurantImages),
                DateTimeUtil.formatToDate(reservation.getReservationDate()),
                DateTimeUtil.formatToHHmm(reservation.getReservationTime().getTimeSlot()),
                reservation.getStatus(),
                DateTimeValidator.isExpired(reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot()),
                SeatTypeInfoResponse.of(reservation.getSeatType())
        );
    }
}
