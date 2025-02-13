package com.trinity.ctc.domain.seat.dto;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 좌석 상세
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupedSeatResponse {
    private final long seatTypeId;
    private final int minCapacity;
    private final int maxCapacity;
    private final int availableSeats;

    public static GroupedSeatResponse of(SeatAvailability seatAvailability) {
        return new GroupedSeatResponse(
                seatAvailability.getSeatType().getId(),
                seatAvailability.getSeatType().getMinCapacity(),
                seatAvailability.getSeatType().getMaxCapacity(),
                seatAvailability.getAvailableSeats()
        );
    }
}
