package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 좌석 상세
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "좌석 상세 정보")
public class GroupedSeatResponse {

    @Schema(description = "예약가능정보 ID", example = "1")
    private final long seatAvailabilityId;

    @Schema(description = "좌석 타입 ID", example = "1")
    private final long seatTypeId;

    @Schema(description = "최소 수용 인원", example = "2")
    private final int minCapacity;

    @Schema(description = "최대 수용 인원", example = "6")
    private final int maxCapacity;

    @Schema(description = "예약 가능한 좌석 수", example = "4")
    private final int availableSeats;

    public static GroupedSeatResponse of(SeatAvailability seatAvailability) {
        return new GroupedSeatResponse(
                seatAvailability.getId(),
                seatAvailability.getSeatType().getId(),
                seatAvailability.getSeatType().getMinCapacity(),
                seatAvailability.getSeatType().getMaxCapacity(),
                seatAvailability.getAvailableSeats()
        );
    }
}
