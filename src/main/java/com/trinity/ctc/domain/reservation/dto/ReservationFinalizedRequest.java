package com.trinity.ctc.domain.reservation.dto;

import lombok.Getter;

@Getter
public class ReservationFinalizedRequest {
    private Long userId;
    private Long reservationId;
}
