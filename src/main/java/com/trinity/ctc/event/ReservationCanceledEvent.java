package com.trinity.ctc.event;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCanceledEvent {
    private final Reservation reservation;
    private final SeatAvailability seatAvailability;
    private final boolean isCODPassed;
}
