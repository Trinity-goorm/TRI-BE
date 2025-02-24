package com.trinity.ctc.event;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCanceledEvent {
    private final Reservation reservation;
    private final Seat seat;
    private final boolean isCODPassed;
}
