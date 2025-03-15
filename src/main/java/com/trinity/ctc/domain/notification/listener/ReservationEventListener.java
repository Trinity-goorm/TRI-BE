package com.trinity.ctc.domain.notification.listener;

import com.trinity.ctc.domain.notification.service.ConfirmationNotificationService;
import com.trinity.ctc.domain.notification.service.ReservationNotificationService;
import com.trinity.ctc.domain.notification.service.SeatNotificationService;
import com.trinity.ctc.domain.reservation.event.PreOccupancyCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final ReservationNotificationService reservationNotificationService;
    private final SeatNotificationService seatNotificationService;
    private final ConfirmationNotificationService confirmationNotificationService;

    @Async
    @EventListener
    public void handleReservationSuccessEvent(ReservationCompleteEvent reservationEvent) {
        reservationNotificationService.registerReservationNotification(reservationEvent.getUserId(), reservationEvent.getReservationId());
        confirmationNotificationService.sendReservationSuccessNotification(reservationEvent.getUserId(), reservationEvent.getReservationId());
    }

    @Async
    @EventListener
    public void handleReservationCanceledEvent(ReservationCanceledEvent reservationEvent) {
        if (reservationEvent.getAvailableSeats() == 1)
            seatNotificationService.sendSeatNotification(reservationEvent.getSeatId());
        confirmationNotificationService.sendReservationCanceledNotification(reservationEvent.getUserId(),
                reservationEvent.getReservationId(), reservationEvent.isCODPassed());
        reservationNotificationService.deleteReservationNotification(reservationEvent.getReservationId());
    }

    @Async
    @EventListener
    public void handlePreOccupancyCanceledEvent(PreOccupancyCanceledEvent preOccupancyCanceledEvent) {
//        빈자리 알림 발송
        if (preOccupancyCanceledEvent.getAvailableSeats() == 1)
            seatNotificationService.sendSeatNotification(preOccupancyCanceledEvent.getSeatId());
    }
}

