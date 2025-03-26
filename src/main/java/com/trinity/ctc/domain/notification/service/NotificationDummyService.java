package com.trinity.ctc.domain.notification.service;

import com.trinity.ctc.domain.notification.Factory.NotificationFactory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.repository.NotificationDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDummyService {
    private final NotificationDummyRepository notificationDummyRepository;
    private final NotificationFactory notificationFactory;

    @Transactional
    public void generateReservationNotifications(List<Map<String, String>> data, int batchSize) {
        log.info("✅ [NotificationDummyService] Reservation CSV 파싱 시작");
        List<ReservationNotification> notifications = notificationFactory.createReservationNotificationsByCsv(data);
        log.info("✅ [NotificationDummyService] 생성된 reservationNotification 개수: {}", notifications.size());

        notificationDummyRepository.batchInsertReservationNotifications(notifications, batchSize);
        log.info("✅ [NotificationDummyService] ReservationNotification 배치 저장 완료");
    }

    @Transactional
    public void generateSeatNotifications(List<Map<String, String>> data, int batchSize) {
        log.info("✅ [NotificationDummyService] SeatNotification CSV 파싱 시작");
        List<SeatNotification> notifications = notificationFactory.createSeatNotificationsByCsv(data);
        log.info("✅ [NotificationDummyService] 생성된 seatNotification 개수: {}", notifications.size());

        notificationDummyRepository.batchInsertSeatNotifications(notifications, batchSize);
        log.info("✅ [NotificationDummyService] SeatNotification 배치 저장 완료");
    }

    @Transactional
    public void generateSeatNotificationSubscriptions(List<Map<String, String>> data, int batchSize) {
        log.info("✅ [NotificationDummyService] SeatNotificationSubscription CSV 파싱 시작");
        List<SeatNotificationSubscription> subscriptions = notificationFactory.createSeatNotificationSubscriptionsByCsv(data);
        log.info("✅ [NotificationDummyService] 생성된 seatNotificationSubscription 개수: {}", subscriptions.size());

        notificationDummyRepository.batchInsertSeatNotificationSubscriptions(subscriptions, batchSize);
        log.info("✅ [NotificationDummyService] SeatNotificationSubscription 배치 저장 완료");
    }
}
