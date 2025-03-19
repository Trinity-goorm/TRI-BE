package com.trinity.ctc.domain.reservation.event;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;

//@Component
//@ShellComponent
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "spring.shell.interactive.enabled", havingValue = "true", matchIfMissing = false)
public class TestEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @ShellMethod(key = "trigger-poc-event", value = "예약선점 취소 이벤트 발행")
    public void triggerPreOccupancyCanceledEvent(@ShellOption long reservationId, @ShellOption long seatId) {
        eventPublisher.publishEvent(new PreOccupancyCanceledEvent(reservationId, seatId, 1));
        System.out.println("✅ 예약선점 취소 이벤트 트리거 실행 완료!");
    }

    @ShellMethod(key = "trigger-complete-event", value = "예약 완료 이벤트 발행")
    public void triggerReservationCompleteEvent(@ShellOption long userId, @ShellOption long reservationId) {
        eventPublisher.publishEvent(new ReservationCompletedEvent(userId, reservationId));
        System.out.println("✅ 예약 완료 이벤트 트리거 실행 완료!");
    }

    @ShellMethod(key = "trigger-cancel-event", value = "예약 취소 이벤트 발행")
    public void triggerReservationCanceledEvent(@ShellOption long userId, @ShellOption long reservationId, @ShellOption long seatId) {
        eventPublisher.publishEvent(new ReservationCanceledEvent(userId, reservationId, seatId, 1, false));
        System.out.println("✅ 예약 취소 이벤트 트리거 실행 완료!");
    }
}
