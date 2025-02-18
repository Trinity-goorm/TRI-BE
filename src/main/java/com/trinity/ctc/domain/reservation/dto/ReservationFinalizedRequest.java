package com.trinity.ctc.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 예약 알림 기능 구현을 위한 임시 구현 requestDTO
 * 예약확정/결제 단계에서 필요한 request param을 담고 있음
 */
@Getter
@AllArgsConstructor
public class ReservationFinalizedRequest {
    private Long userId;
    private Long reservationId;
}
