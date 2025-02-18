package com.trinity.ctc.event;

import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import lombok.Getter;

@Getter
public class ReservationCanceledEvent {
    private final Long reservationId;
    private final SeatAvailability seatAvailability;

    public ReservationCanceledEvent(Long reservationId, SeatAvailability seatAvailability) {
        this.reservationId = reservationId;
        this.seatAvailability = seatAvailability;
    }
}
