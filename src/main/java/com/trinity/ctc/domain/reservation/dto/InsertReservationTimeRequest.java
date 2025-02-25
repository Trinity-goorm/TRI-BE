package com.trinity.ctc.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Schema(description = "예약시간 세팅 요청")
public class InsertReservationTimeRequest {

    @Schema(description = "예약시간 타입슬롯 (HH:mm)", example = "09:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime timeSlot;
}
