package com.trinity.ctc.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;

@Component
@ShellComponent
@RequiredArgsConstructor
public class TestNotificationService {

    private final SeatNotificationService seatNotificationService;
    private final ReservationNotificationService reservationNotificationService;
    private final ConfirmationNotificationService confirmationNotificationService;

    @ShellMethod(key = "send-seat", value = "빈자리 알림 발송")
    public void sendSeatNotification(@ShellOption long seatId) {
        seatNotificationService.sendSeatNotification(seatId);
        System.out.println("✅ 빈자리 알림 발송 완료!");
    }

    @ShellMethod(key = "send-daily", value = "당일 예약 알림 발송")
    public void sendDailyNotification(@ShellOption long reservationId) {
        reservationNotificationService.sendDailyNotification();
        System.out.println("✅ 당일 예약 알림 발송 완료!");
    }

    @ShellMethod(key = "send-hourly", value = "한시간 전 예약 알림 발송")
    public void sendHourBeforeNotification(@ShellOption long reservationId) {
        reservationNotificationService.sendHourBeforeNotification();
        System.out.println("✅ 한시간 전 예약 알림 발송 완료!");
    }

    @ShellMethod(key = "send-complete", value = "예약 완료 확인 알림 발송")
    public void sendReservationCompletedNotification(@ShellOption long userId, @ShellOption long reservationId) {
        confirmationNotificationService.sendReservationCompletedNotification(userId, reservationId);
        System.out.println("✅ 예약 완료 확인 알림 발송 완료!");
    }

    @ShellMethod(key = "send-cancel", value = "예약 취소 확인 알림 발송")
    public void sendReservationCanceledNotification(@ShellOption long userId, @ShellOption long reservationId) {
        confirmationNotificationService.sendReservationCanceledNotification(userId, reservationId, true);
        System.out.println("✅ 예약 취소 확인 알림 발송 완료!");
    }
}
