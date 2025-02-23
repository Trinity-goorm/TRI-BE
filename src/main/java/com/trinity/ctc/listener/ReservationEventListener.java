package com.trinity.ctc.listener;

import com.trinity.ctc.domain.notification.service.NotificationService;
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
        notificationService.registerReservationNotification(reservationEvent.getReservation().getUser(), reservationEvent.getReservation());
        notificationService.sendReservationSuccessNotification(reservationEvent.getReservation().getUser(), reservationEvent.getReservation());
    }

    @Async
    @EventListener
    public void handleReservationCanceledEvent(ReservationCanceledEvent reservationEvent) {
//        빈자리 알림 발송 -> 빈자리 여부 검증 방식...?
        if(reservationEvent.getSeat().getAvailableSeats() == 1) notificationService.sendSeatNotification(reservationEvent.getSeat().getId());
        notificationService.deleteReservationNotification(reservationEvent.getReservation().getId());
        notificationService.sendReservationCanceledNotification(reservationEvent.getReservation().getUser(), reservationEvent.getReservation(), reservationEvent.isCODPassed());
    }

    @Async
    @EventListener
    public void handlePreOccupancyCanceledEvent(PreOccupancyCanceledEvent preOccupancyCanceledEvent) {
//        빈자리 알림 발송
        if(preOccupancyCanceledEvent.getSeat().getAvailableSeats() == 1) notificationService.sendSeatNotification(preOccupancyCanceledEvent.getSeat().getId());
    }
}

