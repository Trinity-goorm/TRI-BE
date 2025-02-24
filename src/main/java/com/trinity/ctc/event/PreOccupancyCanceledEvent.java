package com.trinity.ctc.event;

import com.trinity.ctc.domain.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreOccupancyCanceledEvent {
    private final Long reservationId;
    private final Seat seat;
}
