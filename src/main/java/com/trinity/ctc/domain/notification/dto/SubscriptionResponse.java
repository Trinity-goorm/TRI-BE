package com.trinity.ctc.domain.notification.dto;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@Schema(description = "알림 신청 좌석 정보")
public class SubscriptionResponse {

    @Schema(description = "빈자리 알림 ID", example = "1")
    private long seatNotificationId;

    @Schema(description = "식당 이름", example = "이가네양꼬치")
    private String restaurantName;

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
                seatAvailability.getReservationDate(),
                seatAvailability.getReservationTime().getTimeSlot(),
                seatAvailability.getSeatType().getMinCapacity(),
                seatAvailability.getSeatType().getMaxCapacity(),
                subscriberCount
        );
    }
}
