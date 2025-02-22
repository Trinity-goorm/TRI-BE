package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.domain.seat.entity.SeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "좌석정보 응답")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SeatTypeInfoResponse {

    @Schema(description = "최소인원", example = "1")
    private final int minCapacity;

    @Schema(description = "최대인원", example = "2")
    private final int maxCapacity;

    public static SeatTypeInfoResponse of(SeatType seatType) {
        return new SeatTypeInfoResponse(seatType.getMinCapacity(), seatType.getMaxCapacity());
    }
}
