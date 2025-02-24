package com.trinity.ctc.event;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCompleteEvent {
    private final Reservation reservation;
}