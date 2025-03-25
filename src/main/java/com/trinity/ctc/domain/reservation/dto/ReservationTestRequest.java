package com.trinity.ctc.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Schema(description = "예약정보 전달 테스트용")
public class ReservationTestRequest {

    @Schema(description = "카카오 ID", example = "12345")
    private Long kakaoId;

    @Schema(description = "대상 식당 ID", example = "2")
    private long restaurantId;

    @Schema(description = "좌석타입 ID", example = "1")
    private long seatTypeId;

    @Schema(description = "선택한 예약일 (yyyy-MM-dd)", example = "2025-02-18")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate selectedDate;

    @Schema(description = "예약시간 타입슬롯 (HH:mm)", example = "09:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;
}
