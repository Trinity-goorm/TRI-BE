package com.trinity.ctc.listener;

import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.service.NotificationService;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.event.PreOccupancyCanceledEvent;
import com.trinity.ctc.event.ReservationCanceledEvent;
import com.trinity.ctc.event.ReservationCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleReservationSuccessEvent(ReservationCompleteEvent reservationEvent) {
        notificationService.registerReservationNotification(reservationEvent.getUserId(), reservationEvent.getReservationId());
        notificationService.sendReservationSuccessNotification(reservationEvent.getUserId(), reservationEvent.getReservationId());
    }

    @Async
    @EventListener
    public void handleReservationCanceledEvent(ReservationCanceledEvent reservationEvent) {
        if(reservationEvent.getAvailableSeats() == 1) notificationService.sendSeatNotification(reservationEvent.getSeatId());
        notificationService.sendReservationCanceledNotification(reservationEvent.getUserId(),
                                                                reservationEvent.getReservationId(), reservationEvent.isCODPassed());
        notificationService.deleteReservationNotification(reservationEvent.getReservationId());
    }

    @Async
    @EventListener
    public void handlePreOccupancyCanceledEvent(PreOccupancyCanceledEvent preOccupancyCanceledEvent) {
//        빈자리 알림 발송
        if(preOccupancyCanceledEvent.getAvailableSeats() == 1) notificationService.sendSeatNotification(preOccupancyCanceledEvent.getSeatId());
    }
}

