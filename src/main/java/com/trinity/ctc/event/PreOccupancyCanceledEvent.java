package com.trinity.ctc.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreOccupancyCanceledEvent {
    private final Long reservationId;
    private final Long SeatId;
    private final int availableSeats;
}
