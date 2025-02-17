package com.trinity.ctc.listener;

import com.trinity.ctc.event.ReservationCanceledEvent;
import com.trinity.ctc.event.ReservationSuccessEvent;
import com.trinity.ctc.domain.notification.service.NotificationService;
import com.trinity.ctc.kakao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener
    public void handleReservationSuccessEvent(ReservationSuccessEvent reservationEvent) {
        notificationService.registerReservationNotification(reservationEvent);
    }

    @Async
    @TransactionalEventListener
    public void handleReservationCanceledEvent(ReservationCanceledEvent reservationEvent) {
    }

}

