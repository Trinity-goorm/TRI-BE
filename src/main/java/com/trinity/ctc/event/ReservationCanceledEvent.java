package com.trinity.ctc.event;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import lombok.Getter;

@Getter
public class ReservationCanceledEvent {
    private final Long userId;
    private final Reservation reservation;

    public ReservationCanceledEvent(long userId, Reservation reservation) {
        this.userId = userId;
        this.reservation = reservation;
    }
}
