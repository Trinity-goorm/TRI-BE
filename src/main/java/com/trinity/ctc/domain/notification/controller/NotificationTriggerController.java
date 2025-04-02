package com.trinity.ctc.domain.notification.controller;

import com.trinity.ctc.domain.notification.service.TestNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationTriggerController {

    private final TestNotificationService testNotificationService;

    // 1. 빈자리 알림
    @PostMapping("trigger/send/seat")
    public void sendSeatNotification(@RequestParam long seatId) {
        testNotificationService.sendSeatNotification(seatId);
    }

    // 2. 당일 예약 알림
    @PostMapping("trigger/send/daily")
    public void sendDailyNotification(@RequestParam String date) {
        testNotificationService.sendDailyNotification(date);
    }

    // 3. 한 시간 전 예약 알림
    @PostMapping("trigger/send/hourly")
    public void sendHourlyNotification(@RequestParam String dateTime) {
        testNotificationService.sendHourBeforeNotification(dateTime);
    }

    // 4. 예약 완료 알림
    @PostMapping("trigger/send/complete")
    public void sendReservationComplete(@RequestParam long userId, @RequestParam long reservationId) {
        testNotificationService.sendReservationCompletedNotification(userId, reservationId);
    }

    // 5. 예약 취소 알림
    @PostMapping("trigger/send/cancel")
    public void sendReservationCancel(@RequestParam long userId, @RequestParam long reservationId) {
        testNotificationService.sendReservationCanceledNotification(userId, reservationId);
    }
}
