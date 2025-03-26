package com.trinity.ctc.domain.reservation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCompletedEvent {
    private final Long userId;
    private final Long reservationId;
}