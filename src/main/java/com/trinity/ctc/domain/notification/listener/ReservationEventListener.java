package com.trinity.ctc.domain.notification.listener;

import com.trinity.ctc.domain.notification.service.ConfirmationNotificationService;
import com.trinity.ctc.domain.notification.service.ReservationNotificationService;
import com.trinity.ctc.domain.notification.service.SeatNotificationService;
import com.trinity.ctc.domain.reservation.event.PreOccupancyCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.trinity.ctc.domain.seat.validator.CapacityValidator.checkEmptySeat;

// 예약 관련 이벤트 발생에 대한 처리를 하는 이벤트 리스너
// 이벤트 발생 시, 실행되는 메서드가 알림 관련 메서드 밖에 없어서 notification 패키지 하위에 위치
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final ReservationNotificationService reservationNotificationService;
    private final SeatNotificationService seatNotificationService;
    private final ConfirmationNotificationService confirmationNotificationService;

    /**
     * 예약 완료 이벤트에 대한 핸들러
     * @param reservationCompletedEvent 예약 완료 이벤트
     */
    @Async("reservation-event-listener")
    @EventListener
    public void handleReservationCompletedEvent(ReservationCompletedEvent reservationCompletedEvent) {
        // 예약 완료 알림 발송
        confirmationNotificationService.sendReservationCompletedNotification(reservationCompletedEvent.getUserId(), reservationCompletedEvent.getReservationId());
        // 예약 확인 알림(당일 알림, 1시간 전 알림)
        reservationNotificationService.registerReservationNotification(reservationCompletedEvent.getUserId(), reservationCompletedEvent.getReservationId());
    }

    /**
     * 예약 취소 이벤트에 대한 핸들러
     * @param reservationCanceledEvent 예약 취소 이벤트
     */
    @Async("reservation-event-listener")
    @EventListener
    public void handleReservationCanceledEvent(ReservationCanceledEvent reservationCanceledEvent) {
        // 이벤트를 발행한 대상 예약 자리가 빈자리인지 확인
        if (checkEmptySeat(reservationCanceledEvent.getAvailableSeatsBefore(), reservationCanceledEvent.getAvailableSeatsAfter())) {
            // 빈자리 알림 발송
            seatNotificationService.sendSeatNotification(reservationCanceledEvent.getSeatId());
        }
        // 예약 취소 알림 발송
        confirmationNotificationService.sendReservationCanceledNotification(reservationCanceledEvent.getUserId(),
                reservationCanceledEvent.getReservationId(), reservationCanceledEvent.isCODPassed());
        // 해당 예약과 관련된 예약 확인 알림(당일 알림, 1시간 전 알림) 삭제
        reservationNotificationService.deleteReservationNotification(reservationCanceledEvent.getReservationId());
    }

    /**
     * 예약 선점 취소 이벤트에 대한 핸들러
     * @param preOccupancyCanceledEvent 예약 선점 취소 이벤트
     */
    @Async("reservation-event-listener")
    @EventListener
    public void handlePreOccupancyCanceledEvent(PreOccupancyCanceledEvent preOccupancyCanceledEvent) {
        // 이벤트를 발행한 대상 예약 자리가 빈자리인지 확인
        if (checkEmptySeat(preOccupancyCanceledEvent.getAvailableSeatBefore(), preOccupancyCanceledEvent.getAvailableSeatsAfter())) {
            // 빈자리 알림 발송
            seatNotificationService.sendSeatNotification(preOccupancyCanceledEvent.getSeatId());
        }
    }
}

