package com.trinity.ctc.domain.notification.scheduler;

import com.trinity.ctc.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationService notificationService;

    /**
     * 매일 8시에 당일 예약 알림을 보내는 메서드 호출(매일 8시에 실행되도록 스케줄링)
     */
    @Scheduled(cron = "0 0 8 * * ?") // 매일 8시에 실행
    public void sendDailyNotification() {
        notificationService.sendDailyNotification();
    }

    /**
     * 예약 1시간 전 알림을 보내는 메서드 호출(운영시간(현재 9 ~ 19시) 기준으로 1시간 단위로 실행되도록 스케줄링)
     */
    @Scheduled(cron = "0 0 8-18/1 * * ?") // 8 ~ 18시 내에서 1시간 단위로 실행
    public void sendHourBeforeNotification() {
        notificationService.sendHourBeforeNotification();
    }

    @Scheduled(cron = "0 0 9-19/1 * * ?")
    public void deleteSeatNotificationMessages() {
        notificationService.deleteSeatNotificationMessages();
    }

}
