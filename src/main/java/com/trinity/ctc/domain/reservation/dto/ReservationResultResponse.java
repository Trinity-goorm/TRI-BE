package com.trinity.ctc.domain.reservation.dto;

import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Schema(description = "예약완료 시 정보 반환")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationResultResponse {

    @Schema(description = "예약성공여부", example = "true")
    private final boolean success;

    @Schema(description = "생성된 예약정보 ID", example = "1")
    private final long reservationId;

    @Schema(description = "예약대상 식당", example = "캐치핑 식당")
    private final String restaurantName;

    @Schema(description = "예약날짜", example = "2025-02-15")
    private final String reservationDate;

    @Schema(description = "예약시간 (HH:mm)", example = "09:00")
    private final String reservationTime;

    @Schema(description = "티켓 반환 여부", example = "true", nullable = true)
    private final Boolean isTicketReturned;

    public static ReservationResultResponse of(boolean success, long reservationId, String restaurantName, LocalDate reservationDate, LocalTime reservationTime) {
        return new ReservationResultResponse(success, reservationId, restaurantName, DateTimeUtil.formatToDate(reservationDate), DateTimeUtil.formatToHHmm(reservationTime), null);
    }

    public static ReservationResultResponse of(boolean success, long reservationId, String restaurantName, LocalDate reservationDate, LocalTime reservationTime, boolean ticketReturned) {
        return new ReservationResultResponse(success, reservationId, restaurantName, DateTimeUtil.formatToDate(reservationDate), DateTimeUtil.formatToHHmm(reservationTime), ticketReturned);
    }
}
