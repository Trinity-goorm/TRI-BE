package com.trinity.ctc.event;

import com.trinity.ctc.domain.reservation.dto.ReservationFinalizedRequest;
import lombok.Getter;

@Getter
public class ReservationSuccessEvent {
    private final Long userId;
    private final Long reservationId;

    public ReservationSuccessEvent(ReservationFinalizedRequest request) {
        this.userId = request.getUserId();
        this.reservationId = request.getReservationId();
    }
}