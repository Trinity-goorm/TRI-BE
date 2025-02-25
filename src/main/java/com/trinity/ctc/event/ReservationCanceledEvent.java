package com.trinity.ctc.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCanceledEvent {
    private final Long userId;
    private final Long reservationId;
    private final Long seatId;
    private final int availableSeats;
    private final boolean isCODPassed;
}
